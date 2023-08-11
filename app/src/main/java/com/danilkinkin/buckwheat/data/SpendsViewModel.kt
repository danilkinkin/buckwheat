package com.danilkinkin.buckwheat.data

import androidx.lifecycle.*
import com.danilkinkin.buckwheat.data.entities.Spent
import com.danilkinkin.buckwheat.data.entities.Storage
import com.danilkinkin.buckwheat.di.DatabaseRepository
import com.danilkinkin.buckwheat.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

enum class RestedBudgetDistributionMethod { REST, ADD_TODAY, ASK }

@HiltViewModel
class SpendsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val db: DatabaseRepository,
) : ViewModel() {
    val spentDao = db.spentDao()
    val storageDao = db.storageDao()

    var budget = MutableLiveData(storageDao.getAsBigDecimal("budget", 0.toBigDecimal()))
    var spent = MutableLiveData(storageDao.getAsBigDecimal("spent", 0.0.toBigDecimal()))
    var dailyBudget = MutableLiveData(
        storageDao.getAsBigDecimal("dailyBudget", 0.0.toBigDecimal())
    )
    var spentFromDailyBudget = MutableLiveData(
        storageDao.getAsBigDecimal("spentFromDailyBudget", 0.0.toBigDecimal())
    )

    var restedBudgetDistributionMethod = try {
        MutableLiveData(RestedBudgetDistributionMethod.valueOf(storageDao.get("recalcRestBudgetMethod").value))
    } catch (e: Exception) {
        MutableLiveData(RestedBudgetDistributionMethod.ASK)
    }
    var hideOverspendingWarn = MutableLiveData(
        storageDao.getAsBoolean("overspendingWarnHidden", false)
    )
    var startPeriodDate: MutableLiveData<Date> = MutableLiveData(
        storageDao.getAsDate("startDate", Date())
    )
    var finishPeriodDate = MutableLiveData(storageDao.getAsDate("finishDate", null))
    var lastChangeDailyBudgetDate = storageDao.getAsDate("lastReCalcBudgetDate", null)
    var currency: MutableLiveData<ExtendCurrency> = try {
        MutableLiveData(ExtendCurrency.getInstance(storageDao.get("currency").value))
    } catch (e: Exception) {
        MutableLiveData(ExtendCurrency(value = null, type = CurrencyType.NONE))
    }

    var requireDistributionRestedBudget = MutableLiveData(false)
    var requireSetBudget = MutableLiveData(false)
    var periodFinished = MutableLiveData(false)
    var lastRemovedSpent: MutableSharedFlow<Spent> = MutableSharedFlow()

    init {
        runChangeDayAction()
        runScheduledDetectChangeDayTask()
    }

    fun getSpends(): LiveData<List<Spent>> = this.spentDao.getAll()

    fun changeDisplayCurrency(currency: ExtendCurrency) {
        this.currency.value = currency

        storageDao.set(Storage("currency", currency.value.toString()))
    }

    fun changeRestedBudgetDistributionMethod(method: RestedBudgetDistributionMethod) {
        this.restedBudgetDistributionMethod.value = method

        storageDao.set(Storage("recalcRestBudgetMethod", method.toString()))
    }

    fun changeBudget(newBudget: BigDecimal, newFinishDate: Date) {
        // Reset all data
        budget.value = newBudget
        startPeriodDate.value = roundToDay(Date())
        finishPeriodDate.value = Date(roundToDay(newFinishDate).time + DAY - 1000)
        spent.value = 0.0.toBigDecimal()
        dailyBudget.value = 0.0.toBigDecimal()
        spentFromDailyBudget.value = 0.0.toBigDecimal()
        lastChangeDailyBudgetDate = startPeriodDate.value
        requireSetBudget.value = false
        periodFinished.value = false
        hideOverspendingWarn(false)
        spentDao.deleteAll()

        // Save new data to storage
        storageDao.set(Storage("budget", budget.toString()))
        storageDao.set(Storage("startDate", startPeriodDate.toString()))
        storageDao.set(Storage("finishDate", finishPeriodDate.toString()))
        storageDao.set(Storage("spent", spent.toString()))
        storageDao.set(Storage("dailyBudget", dailyBudget.toString()))
        storageDao.set(Storage("spentFromDailyBudget", spentFromDailyBudget.toString()))
        storageDao.set(Storage("lastReCalcBudgetDate", startPeriodDate.toString()))

        setDailyBudget(whatBudgetForDay())
    }

    fun whatBudgetForDay(
        excludeCurrentDay: Boolean = false,
        notCommittedSpent: BigDecimal = 0.0.toBigDecimal()
    ): BigDecimal {
        val restDays = countDaysToToday(finishPeriodDate.value!!) - if (excludeCurrentDay) 1 else 0
        var restBudget = budget.value!! - spent.value!!

        if (!excludeCurrentDay) {
            restBudget -= notCommittedSpent
        }

        restBudget -= if (excludeCurrentDay) {
            dailyBudget.value!!
        } else {
            spentFromDailyBudget.value!!
        }

        return restBudget
            .divide(
                restDays.toBigDecimal().coerceAtLeast(BigDecimal(1)),
                0,
                RoundingMode.FLOOR
            )
    }

    fun howMuchNotSpent(): BigDecimal {
        val restDays = countDaysToToday(finishPeriodDate.value!!)
        val skippedDays = abs(countDaysToToday(lastChangeDailyBudgetDate!!))
        val restBudget = budget.value!! - spent.value!! - dailyBudget.value!!

        return restBudget
            .divide((restDays + skippedDays - 1).coerceAtLeast(1).toBigDecimal())
            .multiply((skippedDays - 1).coerceAtLeast(0).toBigDecimal())
            .plus(dailyBudget.value!! - spentFromDailyBudget.value!!)
    }

    fun howMuchBudgetRest(): BigDecimal {
        return budget.value!! - spent.value!! - spentFromDailyBudget.value!!
    }

    fun setDailyBudget(newDailyBudget: BigDecimal) {
        dailyBudget.value = newDailyBudget
        lastChangeDailyBudgetDate = roundToDay(Date())
        spent.value = spent.value!! + spentFromDailyBudget.value!!
        spentFromDailyBudget.value = BigDecimal(0)

        storageDao.set(Storage("spent", spent.toString()))
        storageDao.set(Storage("dailyBudget", dailyBudget.toString()))
        storageDao.set(Storage("spentFromDailyBudget", spentFromDailyBudget.toString()))
        storageDao.set(Storage("lastReCalcBudgetDate", lastChangeDailyBudgetDate.toString()))
    }

    fun addSpent(newSpent: Spent) {
        this.spentDao.insert(newSpent)

        if (isToday(newSpent.date)) {
            spentFromDailyBudget.value = spentFromDailyBudget.value?.plus(newSpent.value)

            storageDao.set(Storage("spentFromDailyBudget", spentFromDailyBudget.value.toString()))
        } else {
            val spreadDeltaSpentPerRestDays = newSpent.value
                .divide(countDays(finishPeriodDate.value!!, newSpent.date).toBigDecimal())

            dailyBudget.value = dailyBudget.value!! + spreadDeltaSpentPerRestDays
            spent.value = spent.value!! + newSpent.value

            storageDao.set(Storage("dailyBudget", dailyBudget.value.toString()))
            storageDao.set(Storage("spent", spent.value.toString()))
        }
    }

    fun removeSpent(spentForRemove: Spent, silent: Boolean = false) {
        this.spentDao.deleteById(spentForRemove.uid)

        if (isToday(spentForRemove.date)) {
            spentFromDailyBudget.value = spentFromDailyBudget.value!! - spentForRemove.value

            storageDao.set(Storage("spentFromDailyBudget", spentFromDailyBudget.value.toString()))
        } else {
            val restDays = countDays(finishPeriodDate.value!!, spentForRemove.date)
            val spreadDeltaSpentPerRestDays = spentForRemove.value / restDays.toBigDecimal()

            spent.value = spent.value!! - spentForRemove.value
            dailyBudget.value = dailyBudget.value!! + spreadDeltaSpentPerRestDays

            storageDao.set(Storage("dailyBudget", dailyBudget.value.toString()))
            storageDao.set(Storage("spent", spent.value.toString()))
        }

        if (!silent) {
            viewModelScope.launch {
                lastRemovedSpent.emit(spentForRemove)
            }
        }
    }

    fun undoRemoveSpent(removedSpent: Spent) {
        this.spentDao.insert(removedSpent)

        if (isToday(removedSpent.date)) {
            spentFromDailyBudget.value = spentFromDailyBudget.value!! + removedSpent.value

            storageDao.set(
                Storage("spentFromDailyBudget", spentFromDailyBudget.value.toString())
            )
        } else {
            val restDays = countDaysToToday(finishPeriodDate.value!!)
            val spreadDeltaSpentPerRestDays = removedSpent.value / restDays.toBigDecimal()

            dailyBudget.value = dailyBudget.value!! - spreadDeltaSpentPerRestDays
            spent.value = spent.value!! + (removedSpent.value - spreadDeltaSpentPerRestDays)

            storageDao.set(Storage("dailyBudget", dailyBudget.value.toString()))
            storageDao.set(Storage("spent", spent.value.toString()))
        }
    }

    fun hideOverspendingWarn(hide: Boolean) {
        this.hideOverspendingWarn.value = hide

        storageDao.set(Storage("overspendingWarnHidden", hide.toString()))
    }

    private fun runChangeDayAction() {
        when {
            lastChangeDailyBudgetDate !== null
                    && !isToday(lastChangeDailyBudgetDate!!)
                    && countDaysToToday(finishPeriodDate.value!!) > 0 -> {
                if (dailyBudget.value!! - spentFromDailyBudget.value!! > BigDecimal(0)) {
                    when (restedBudgetDistributionMethod.value) {
                        RestedBudgetDistributionMethod.ASK, null -> requireDistributionRestedBudget.value =
                            true

                        RestedBudgetDistributionMethod.REST -> setDailyBudget(whatBudgetForDay())
                        RestedBudgetDistributionMethod.ADD_TODAY -> setDailyBudget(
                            whatBudgetForDay(excludeCurrentDay = true) + howMuchNotSpent()
                        )
                    }
                } else {
                    setDailyBudget(whatBudgetForDay())
                }
            }

            lastChangeDailyBudgetDate === null -> {
                requireSetBudget.value = true
            }

            finishPeriodDate.value!!.time <= Date().time -> {
                periodFinished.value = true
            }
        }

        // Bug fix https://github.com/danilkinkin/buckwheat/issues/28
        if (this.dailyBudget.value!! - this.spentFromDailyBudget.value!! > BigDecimal(0)) {
            hideOverspendingWarn(false)
        }
    }

    private fun runScheduledDetectChangeDayTask() {
        var currentDay = Date()

        viewModelScope.launch {
            while (true) {
                delay(5000L)

                if (isToday(currentDay)) continue

                currentDay = Date()
                runChangeDayAction()
            }
        }
    }
}
