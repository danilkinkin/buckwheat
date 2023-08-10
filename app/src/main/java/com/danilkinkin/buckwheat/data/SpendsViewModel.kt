package com.danilkinkin.buckwheat.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.danilkinkin.buckwheat.data.entities.Spent
import com.danilkinkin.buckwheat.data.entities.Storage
import com.danilkinkin.buckwheat.di.DatabaseRepository
import com.danilkinkin.buckwheat.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

enum class EditMode { ADD, EDIT }
enum class EditStage { IDLE, CREATING_SPENT, EDIT_SPENT, COMMITTING_SPENT }
enum class RestedBudgetDistributionMethod { REST, ADD_TODAY, ASK }

@HiltViewModel
class SpendsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val db: DatabaseRepository,
) : ViewModel() {

    private val spentDao = db.spentDao()
    private val storageDao = db.storageDao()

    var mode = MutableLiveData(EditMode.ADD)
    var stage = MutableLiveData(EditStage.IDLE)
    var lastRemoveSpent: MutableSharedFlow<Spent> = MutableSharedFlow()

    var budget = MutableLiveData(storageDao.getAsBigDecimal("budget", 0.toBigDecimal()))
    var spent = MutableLiveData(storageDao.getAsBigDecimal("spent", 0.0.toBigDecimal()))
    var dailyBudget = MutableLiveData(
        storageDao.getAsBigDecimal("dailyBudget", 0.0.toBigDecimal())
    )
    var spentFromDailyBudget = MutableLiveData(
        storageDao.getAsBigDecimal("spentFromDailyBudget", 0.0.toBigDecimal())
    )
    var recalcRestBudgetMethod = MutableLiveData(
        try {
            RestedBudgetDistributionMethod.valueOf(storageDao.get("recalcRestBudgetMethod").value)
        } catch (e: Exception) {
            RestedBudgetDistributionMethod.ASK
        }
    )
    var overspendingWarnHidden = MutableLiveData(
        storageDao.getAsBoolean("overspendingWarnHidden", false)
    )
    var startDate: MutableLiveData<Date> =
        MutableLiveData(storageDao.getAsDate("startDate", Date()))
    var finishDate = MutableLiveData(storageDao.getAsDate("finishDate", null))
    var lastReCalcBudgetDate: Date? =
        storageDao.getAsDate("lastReCalcBudgetDate", null)
    var currency: MutableLiveData<ExtendCurrency> = try {
        MutableLiveData(ExtendCurrency.getInstance(storageDao.get("currency").value))
    } catch (e: Exception) {
        MutableLiveData(ExtendCurrency(value = null, type = CurrencyType.NONE))
    }

    var editedSpent: Spent? = null
    var currentDate: Date = Date()
    var currentSpent: BigDecimal = 0.0.toBigDecimal()
    var currentComment: String = ""

    var requireReCalcBudget = MutableLiveData(false)
    var requireSetBudget = MutableLiveData(false)
    var finishPeriod = MutableLiveData(false)

    var rawSpentValue = MutableLiveData("")

    init {
        runChangeDayAction()
        runScheduledDetectChangeDayTask()
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

    private fun runChangeDayAction() {
        if (
            lastReCalcBudgetDate !== null
            && !isToday(lastReCalcBudgetDate!!)
            && countDaysToToday(finishDate.value!!) > 0
        ) {
            if (dailyBudget.value!! - spentFromDailyBudget.value!! > BigDecimal(0)) {
                when (recalcRestBudgetMethod.value) {
                    RestedBudgetDistributionMethod.ASK, null -> requireReCalcBudget.value = true
                    RestedBudgetDistributionMethod.REST -> setDailyBudget(whatBudgetForDay())
                    RestedBudgetDistributionMethod.ADD_TODAY -> setDailyBudget(
                        whatBudgetForDay(excludeCurrentDay = true) + howMuchNotSpent()
                    )
                }
            } else {
                setDailyBudget(whatBudgetForDay())
            }
        } else if (lastReCalcBudgetDate === null) {
            requireSetBudget.value = true
        } else if (finishDate.value!!.time <= Date().time) {
            finishPeriod.value = true
        }

        // Bug fix https://github.com/danilkinkin/buckwheat/issues/28
        if (this.dailyBudget.value!! - this.spentFromDailyBudget.value!! > BigDecimal(0)) {
            hideOverspendingWarn(false)
        }
    }

    fun getSpends(): LiveData<List<Spent>> = this.spentDao.getAll()

    fun changeDisplayCurrency(currency: ExtendCurrency) {
        this.currency.value = currency

        storageDao.set(Storage("currency", currency.value.toString()))
    }

    fun changeRestedBudgetDistributionMethod(method: RestedBudgetDistributionMethod) {
        this.recalcRestBudgetMethod.value = method

        storageDao.set(Storage("recalcRestBudgetMethod", method.toString()))
    }

    fun changeBudget(newBudget: BigDecimal, newFinishDate: Date) {
        // Reset all data
        budget.value = newBudget
        startDate.value = roundToDay(Date())
        finishDate.value = Date(roundToDay(newFinishDate).time + DAY - 1000)
        spent.value = 0.0.toBigDecimal()
        dailyBudget.value = 0.0.toBigDecimal()
        spentFromDailyBudget.value = 0.0.toBigDecimal()
        lastReCalcBudgetDate = startDate.value
        requireSetBudget.value = false
        finishPeriod.value = false
        hideOverspendingWarn(false)
        spentDao.deleteAll()

        // Save new data to storage
        storageDao.set(Storage("budget", budget.toString()))
        storageDao.set(Storage("startDate", startDate.toString()))
        storageDao.set(Storage("finishDate", finishDate.toString()))
        storageDao.set(Storage("spent", spent.toString()))
        storageDao.set(Storage("dailyBudget", dailyBudget.toString()))
        storageDao.set(Storage("spentFromDailyBudget", spentFromDailyBudget.toString()))
        storageDao.set(Storage("lastReCalcBudgetDate", startDate.toString()))

        resetEditingSpent()
        setDailyBudget(whatBudgetForDay())
    }

    fun whatBudgetForDay(
        applyCurrentSpent: Boolean = false,
        excludeCurrentDay: Boolean = false,
    ): BigDecimal {
        val restDays = countDaysToToday(finishDate.value!!) - if (excludeCurrentDay) 1 else 0
        var restBudget = budget.value!! - spent.value!!

        if (applyCurrentSpent && !excludeCurrentDay) {
            restBudget -= currentSpent
        }

        restBudget -= if (excludeCurrentDay) {
            dailyBudget.value!!
        } else {
            spentFromDailyBudget.value!!
        }

        return restBudget
            .divide(restDays.toBigDecimal().coerceAtLeast(BigDecimal(1)))
            .setScale(0, RoundingMode.FLOOR)
    }

    fun howMuchNotSpent(): BigDecimal {
        val restDays = countDaysToToday(finishDate.value!!)
        val skippedDays = abs(countDaysToToday(lastReCalcBudgetDate!!))
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
        lastReCalcBudgetDate = roundToDay(Date())
        spent.value = spent.value!! + spentFromDailyBudget.value!!
        spentFromDailyBudget.value = BigDecimal(0)

        storageDao.set(Storage("spent", spent.toString()))
        storageDao.set(Storage("dailyBudget", dailyBudget.toString()))
        storageDao.set(Storage("spentFromDailyBudget", spentFromDailyBudget.toString()))
        storageDao.set(Storage("lastReCalcBudgetDate", lastReCalcBudgetDate.toString()))
    }

    fun startEditingSpent(spent: Spent) {
        editedSpent = spent
        currentSpent = spent.value
        currentDate = spent.date
        currentComment = spent.comment
        rawSpentValue.value = tryConvertStringToNumber(spent.value.toString()).join(third = false)

        stage.value = EditStage.EDIT_SPENT
        mode.value = EditMode.EDIT
    }

    fun startCreatingSpent() {
        currentSpent = 0.0.toBigDecimal()

        stage.value = EditStage.CREATING_SPENT
    }

    fun modifyEditingSpent(value: BigDecimal) {
        currentSpent = value

        stage.value = EditStage.EDIT_SPENT
    }

    fun resetEditingSpent() {
        currentSpent = 0.0.toBigDecimal()
        currentDate = Date()
        currentComment = ""
        rawSpentValue.value = ""

        stage.value = EditStage.IDLE
        mode.value = EditMode.ADD
        editedSpent = null
    }

    fun commitEditingSpent() {
        if (stage.value !== EditStage.EDIT_SPENT) return

        val formatSpent = currentSpent
            .setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()

        if (formatSpent == "0") return

        if (mode.value == EditMode.EDIT) {
            val newVersionOfSpent = editedSpent!!.copy(
                value = currentSpent,
                date = currentDate,
                comment = currentComment,
            )

            this.removeSpent(editedSpent!!, silent = true)
            this.addSpent(newVersionOfSpent)
        } else {
            this.addSpent(Spent(currentSpent, Date(), currentComment))
        }

        stage.value = EditStage.COMMITTING_SPENT

        resetEditingSpent()
    }

    private fun addSpent(newSpent: Spent) {
        this.spentDao.insert(newSpent)

        if (isToday(newSpent.date)) {
            spentFromDailyBudget.value = spentFromDailyBudget.value?.plus(newSpent.value)

            storageDao.set(Storage("spentFromDailyBudget", spentFromDailyBudget.value.toString()))
        } else {
            val spreadDeltaSpentPerRestDays = newSpent.value
                .divide(countDays(finishDate.value!!, newSpent.date).toBigDecimal())

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
            val restDays = countDays(finishDate.value!!, spentForRemove.date)
            val spreadDeltaSpentPerRestDays = spentForRemove.value / restDays.toBigDecimal()

            spent.value = spent.value!! - spentForRemove.value
            dailyBudget.value = dailyBudget.value!! + spreadDeltaSpentPerRestDays

            storageDao.set(Storage("dailyBudget", dailyBudget.value.toString()))
            storageDao.set(Storage("spent", spent.value.toString()))
        }
        editedSpent = null

        if (!silent) {
            viewModelScope.launch {
                lastRemoveSpent.emit(spentForRemove)
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
            val restDays = countDaysToToday(finishDate.value!!)
            val spreadDeltaSpentPerRestDays = removedSpent.value / restDays.toBigDecimal()

            dailyBudget.value = dailyBudget.value!! - spreadDeltaSpentPerRestDays
            spent.value = spent.value!! + (removedSpent.value - spreadDeltaSpentPerRestDays)

            storageDao.set(Storage("dailyBudget", dailyBudget.value.toString()))
            storageDao.set(Storage("spent", spent.value.toString()))
        }
    }

    fun hideOverspendingWarn(hide: Boolean) {
        this.overspendingWarnHidden.value = hide

        storageDao.set(Storage("overspendingWarnHidden", hide.toString()))
    }

    fun exportAsCsv(context: Context, uri: Uri) {
        val stream = context.contentResolver.openOutputStream(uri)

        val printer = CSVPrinter(
            stream?.writer(),
            CSVFormat.Builder.create().setHeader("amount", "comment", "commit_time").build()
        )
        val dateFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)

        this.spentDao.getAllSync().forEach {
            printer.printRecord(
                it.value,
                it.comment,
                it.date.toLocalDateTime().format(dateFormatter),
            )
        }

        printer.flush()
        printer.close()
        stream?.close()
    }
}
