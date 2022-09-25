package com.danilkinkin.buckwheat.data

import android.util.Log
import androidx.lifecycle.*
import com.danilkinkin.buckwheat.data.entities.Spent
import com.danilkinkin.buckwheat.data.entities.Storage
import com.danilkinkin.buckwheat.di.DatabaseRepository
import com.danilkinkin.buckwheat.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SpendsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val db: DatabaseRepository,
) : ViewModel() {
    enum class Stage { IDLE, CREATING_SPENT, EDIT_SPENT, COMMITTING_SPENT }
    enum class Action { PUT_NUMBER, SET_DOT, REMOVE_LAST }

    private val spentDao = db.spentDao()
    private val storageDao = db.storageDao()

    var stage: MutableLiveData<Stage> = MutableLiveData(Stage.IDLE)
    var lastRemoveSpent: MutableSharedFlow<Spent> = MutableSharedFlow()

    var budget: MutableLiveData<BigDecimal> = MutableLiveData(
        try {
            storageDao.get("budget").value.toBigDecimal()
        } catch (e: Exception) {
            0.0.toBigDecimal()
        }
    )
    var spent: MutableLiveData<BigDecimal> = MutableLiveData(
        try {
            storageDao.get("spent").value.toBigDecimal()
        } catch (e: Exception) {
            0.0.toBigDecimal()
        }
    )
    var dailyBudget: MutableLiveData<BigDecimal> = MutableLiveData(
        try {
            storageDao.get("dailyBudget").value.toBigDecimal()
        } catch (e: Exception) {
            0.0.toBigDecimal()
        }
    )
    var spentFromDailyBudget: MutableLiveData<BigDecimal> = MutableLiveData(
        try {
            storageDao.get("spentFromDailyBudget").value.toBigDecimal()
        } catch (e: Exception) {
            0.0.toBigDecimal()
        }
    )

    var startDate: Date = try {
        Date(storageDao.get("startDate").value.toLong())
    } catch (e: Exception) {
        Date()
    }
    var finishDate: Date = try {
        Date(storageDao.get("finishDate").value.toLong())
    } catch (e: Exception) {
        Date()
    }
    var lastReCalcBudgetDate: Date? = try {
        Date(storageDao.get("lastReCalcBudgetDate").value.toLong())
    } catch (e: Exception) {
        null
    }

    var currency: ExtendCurrency = try {
        ExtendCurrency.getInstance(storageDao.get("currency").value)
    } catch (e: Exception) {
        ExtendCurrency(value = null, type = CurrencyType.NONE)
    }

    var currentSpent: BigDecimal = 0.0.toBigDecimal()

    var requireReCalcBudget: MutableLiveData<Boolean> = MutableLiveData(false)
    var requireSetBudget: MutableLiveData<Boolean> = MutableLiveData(false)

    var valueLeftDot: String = ""
    var valueRightDot: String = ""
    var useDot: Boolean = false

    init {
        if (
            lastReCalcBudgetDate !== null
            && !isSameDay(lastReCalcBudgetDate!!.time, Date().time)
            && countDays(finishDate) > 0
        ) {
            requireReCalcBudget.value = true
        }

        Log.d("SpentViewModel", "init")

        if (lastReCalcBudgetDate === null || finishDate.time <= Date().time) {
            requireSetBudget.value = true
        }
    }

    fun getSpends(): LiveData<List<Spent>> {
        return this.spentDao.getAll()
    }

    fun changeCurrency(currency: ExtendCurrency) {
        storageDao.set(Storage("currency", currency.value.toString()))

        this.currency = currency
    }

    fun changeBudget(budget: BigDecimal, finishDate: Date) {
        storageDao.set(Storage("budget", budget.toString()))
        this.budget.value = budget

        val startDate = roundToDay(Date())
        storageDao.set(Storage("startDate", startDate.time.toString()))
        this.startDate = startDate

        val roundedFinishDate = roundToDay(finishDate)
        storageDao.set(Storage("finishDate", roundedFinishDate.time.toString()))
        this.finishDate = roundedFinishDate

        storageDao.set(Storage("spent", 0.0.toString()))
        this.spent.value = 0.0.toBigDecimal()

        storageDao.set(Storage("dailyBudget", 0.0.toString()))
        this.dailyBudget.value = 0.0.toBigDecimal()

        storageDao.set(Storage("spentFromDailyBudget", 0.0.toString()))
        this.spentFromDailyBudget.value = 0.0.toBigDecimal()

        storageDao.set(Storage("lastReCalcBudgetDate", roundToDay(startDate).time.toString()))
        this.lastReCalcBudgetDate = startDate

        this.spentDao.deleteAll()

        requireSetBudget.value = false

        resetSpent()
        reCalcDailyBudget(
            (budget / countDays(roundedFinishDate).toBigDecimal()).setScale(
                0,
                RoundingMode.FLOOR
            )
        )
    }

    fun reCalcDailyBudget(dailyBudget: BigDecimal) {
        this.dailyBudget.value = dailyBudget
        lastReCalcBudgetDate = roundToDay(Date())
        this.spent.value = this.spent.value!! + spentFromDailyBudget.value!!

        storageDao.set(Storage("spent", this.spent.value.toString()))
        storageDao.set(Storage("dailyBudget", dailyBudget.toString()))
        storageDao.set(Storage("spentFromDailyBudget", 0.0.toString()))
        storageDao.set(Storage("lastReCalcBudgetDate", lastReCalcBudgetDate!!.time.toString()))
    }

    private suspend fun createSpent() {
        Log.d("Main", "createSpent")
        currentSpent = 0.0.toBigDecimal()

        stage.value = Stage.CREATING_SPENT
    }

    private suspend fun editSpent(value: BigDecimal) {
        Log.d("Main", "editSpent")
        currentSpent = value

        stage.value = Stage.EDIT_SPENT
    }

    suspend fun commitSpent() {
        if (stage.value !== Stage.EDIT_SPENT) return

        this.spentDao.insert(Spent(currentSpent, Date()))

        spentFromDailyBudget.value = spentFromDailyBudget.value?.plus(currentSpent)
        storageDao.set(Storage("spentFromDailyBudget", spentFromDailyBudget.value.toString()))

        currentSpent = 0.0.toBigDecimal()
        valueLeftDot = ""
        valueRightDot = ""
        useDot = false

        stage.value = Stage.COMMITTING_SPENT
    }

    fun resetSpent() {
        currentSpent = 0.0.toBigDecimal()
        valueLeftDot = ""
        valueRightDot = ""
        useDot = false

        stage.value = Stage.IDLE
    }

    fun removeSpent(spent: Spent) {
        this.spentDao.markAsDeleted(spent.uid, true)

        spentFromDailyBudget.value = spentFromDailyBudget.value!! - spent.value
        storageDao.set(Storage("spentFromDailyBudget", spentFromDailyBudget.value.toString()))

        viewModelScope.launch {
            lastRemoveSpent.emit(spent)
        }
    }

    fun undoRemoveSpent(spent: Spent) {
        this.spentDao.markAsDeleted(spent.uid, false)

        spentFromDailyBudget.value = spentFromDailyBudget.value!! + spent.value
        storageDao.set(
            Storage(
                "spentFromDailyBudget",
                spentFromDailyBudget.value.toString()
            )
        )
    }

    fun commitDeletedSpends() {
        this.spentDao.commitDeleted()
    }

    fun executeAction(action: Action, value: Int? = null) {
        var mutateSpent = true

        when (action) {
            Action.PUT_NUMBER -> {
                if (useDot) {
                    valueRightDot += value
                } else {
                    valueLeftDot += value
                }
            }
            Action.SET_DOT -> {
                if (useDot) return

                useDot = true
                valueRightDot = ""
                valueLeftDot = if (valueLeftDot === "") {
                    "0"
                } else {
                    valueLeftDot
                }
            }
            Action.REMOVE_LAST -> {
                if ("$valueLeftDot.$valueRightDot" == ".") {
                    mutateSpent = false
                } else if (useDot && valueRightDot.length > 1) {
                    valueRightDot = valueRightDot.dropLast(1)
                } else if (useDot && valueRightDot.length <= 1) {
                    valueRightDot = ""
                    useDot = false
                } else if (valueLeftDot.length > 1) {
                    valueLeftDot = valueLeftDot.dropLast(1)
                    useDot = false
                } else {
                    valueLeftDot = ""
                    useDot = false
                }

                if ("$valueLeftDot.$valueRightDot" == ".") {
                    runBlocking {
                        Log.d("ViewModel", "resetSpent")
                        resetSpent()
                    }

                    return
                }
            }
        }

        if (mutateSpent) {
            runBlocking {
                Log.d("ViewModel", "create/edit")
                if (stage.value === Stage.IDLE) createSpent()
                editSpent("$valueLeftDot.$valueRightDot".toBigDecimal())
            }
        }
    }
}
