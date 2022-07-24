package com.danilkinkin.buckwheat.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.danilkinkin.buckwheat.MainActivity
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.di.DatabaseModule
import com.danilkinkin.buckwheat.entities.Draw
import com.danilkinkin.buckwheat.entities.Storage
import com.danilkinkin.buckwheat.utils.*
import com.google.android.material.snackbar.Snackbar
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.floor

class DrawsViewModel(application: Application) : AndroidViewModel(application) {
    enum class Stage { IDLE, CREATING_DRAW, EDIT_DRAW, COMMITTING_DRAW }
    enum class Action { PUT_NUMBER, SET_DOT, REMOVE_LAST }

    private val db = DatabaseModule.getInstance(application)

    private val draws = db.drawDao()
    private val storage = db.storageDao()

    var stage: MutableLiveData<Stage> = MutableLiveData(Stage.IDLE)

    var budget: MutableLiveData<BigDecimal> = MutableLiveData(try {
        storage.get("budget").value.toBigDecimal()
    } catch (e: Exception) {
        0.0.toBigDecimal()
    })
    var spent: MutableLiveData<BigDecimal> = MutableLiveData(try {
        storage.get("spent").value.toBigDecimal()
    } catch (e: Exception) {
        0.0.toBigDecimal()
    })
    var dailyBudget: MutableLiveData<BigDecimal> = MutableLiveData(try {
        storage.get("dailyBudget").value.toBigDecimal()
    } catch (e: Exception) {
        0.0.toBigDecimal()
    })
    var spentFromDailyBudget: MutableLiveData<BigDecimal> = MutableLiveData(try {
        storage.get("spentFromDailyBudget").value.toBigDecimal()
    } catch (e: Exception) {
        0.0.toBigDecimal()
    })

    var startDate: Date = try {
        Date(storage.get("startDate").value.toLong())
    } catch (e: Exception) {
        Date()
    }
    var finishDate: Date = try {
        Date(storage.get("finishDate").value.toLong())
    } catch (e: Exception) {
        Date()
    }
    var lastReCalcBudgetDate: Date? = try {
        Date(storage.get("lastReCalcBudgetDate").value.toLong())
    } catch (e: Exception) {
        null
    }

    var currency: ExtendCurrency = try {
        ExtendCurrency.getInstance(storage.get("currency").value)
    } catch (e: Exception) {
        ExtendCurrency(value = null, type = CurrencyType.NONE)
    }

    var currentDraw: BigDecimal = 0.0.toBigDecimal()

    var requireReCalcBudget: MutableLiveData<Boolean> = MutableLiveData(false)
    var requireSetBudget: MutableLiveData<Boolean> = MutableLiveData(false)

    var valueLeftDot: String = ""
    var valueRightDot: String = ""
    var useDot: Boolean = false

    init {
        if (lastReCalcBudgetDate !== null && !isSameDay(lastReCalcBudgetDate!!.time, Date().time)) {
            requireReCalcBudget.value = true
        }

        if (lastReCalcBudgetDate === null || finishDate.time <= Date().time) {
            requireSetBudget.value = true
        }
    }

    fun getDraws(): LiveData<List<Draw>> {
        return draws.getAll()
    }

    fun changeCurrency(currency: ExtendCurrency) {
        storage.set(Storage("currency", currency.value.toString()))

        this.currency = currency
    }

    fun changeBudget(budget: BigDecimal, finishDate: Date) {
        storage.set(Storage("budget", budget.toString()))
        this.budget.value = budget

        val startDate = roundToDay(Date())
        storage.set(Storage("startDate", startDate.time.toString()))
        this.startDate = startDate

        val roundedFinishDate = roundToDay(finishDate)
        storage.set(Storage("finishDate", roundedFinishDate.time.toString()))
        this.finishDate = roundedFinishDate

        storage.set(Storage("spent", 0.0.toString()))
        this.spent.value = 0.0.toBigDecimal()

        storage.set(Storage("dailyBudget", 0.0.toString()))
        this.dailyBudget.value = 0.0.toBigDecimal()

        storage.set(Storage("spentFromDailyBudget", 0.0.toString()))
        this.spentFromDailyBudget.value = 0.0.toBigDecimal()

        storage.set(Storage("lastReCalcBudgetDate", roundToDay(startDate).time.toString()))
        this.lastReCalcBudgetDate = startDate

        draws.deleteAll()

        resetDraw()
        reCalcDailyBudget((budget / countDays(roundedFinishDate).toBigDecimal()).setScale(0, RoundingMode.FLOOR))
    }

    fun reCalcDailyBudget(dailyBudget: BigDecimal) {
        this.dailyBudget.value = dailyBudget
        lastReCalcBudgetDate = roundToDay(Date())
        spent.value = spent.value!! + spentFromDailyBudget.value!!

        storage.set(Storage("spent", spent.value.toString()))
        storage.set(Storage("dailyBudget", dailyBudget.toString()))
        storage.set(Storage("spentFromDailyBudget", 0.0.toString()))
        storage.set(Storage("lastReCalcBudgetDate", lastReCalcBudgetDate!!.time.toString()))
    }

    fun createDraw() {
        currentDraw = 0.0.toBigDecimal()

        stage.value = Stage.CREATING_DRAW
    }

    fun editDraw(value: BigDecimal) {
        currentDraw = value

        stage.value = Stage.EDIT_DRAW
    }

    fun commitDraw() {
        if (stage.value !== Stage.EDIT_DRAW) return

        draws.insert(Draw(currentDraw, Date()))

        spentFromDailyBudget.value = spentFromDailyBudget.value?.plus(currentDraw)
        storage.set(Storage("spentFromDailyBudget", spentFromDailyBudget.value.toString()))

        currentDraw = 0.0.toBigDecimal()
        valueLeftDot = ""
        valueRightDot = ""
        useDot = false

        stage.value = Stage.COMMITTING_DRAW
    }

    fun resetDraw() {
        currentDraw = 0.0.toBigDecimal()
        valueLeftDot = ""
        valueRightDot = ""
        useDot = false

        stage.value = Stage.IDLE
    }

    fun removeDraw(draw: Draw) {
        draws.delete(draw)

        spentFromDailyBudget.value = spentFromDailyBudget.value!! - draw.value
        storage.set(Storage("spentFromDailyBudget", spentFromDailyBudget.value.toString()))

        Snackbar
            .make(MainActivity.getInstance().parentView, R.string.remove_draw, Snackbar.LENGTH_LONG)
            .setAction(
                MainActivity
                    .getInstance()
                    .applicationContext
                    .getString(R.string.remove_draw_undo)
                    .uppercase(Locale.getDefault())
            ) {
                draws.insert(draw)

                spentFromDailyBudget.value = spentFromDailyBudget.value!! + draw.value
                storage.set(Storage("spentFromDailyBudget", spentFromDailyBudget.value.toString()))
            }
            .show()
    }

    fun executeAction(action: Action, value: Int? = null) {
        if (stage.value === Stage.IDLE) createDraw()

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
                valueLeftDot = if (valueLeftDot === "") { "0" } else { valueLeftDot }
            }
            Action.REMOVE_LAST -> {
                if (useDot && valueRightDot.length > 1) {
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
                    resetDraw()

                    return
                }
            }
        }

        editDraw("$valueLeftDot.$valueRightDot".toBigDecimal())
    }
}
