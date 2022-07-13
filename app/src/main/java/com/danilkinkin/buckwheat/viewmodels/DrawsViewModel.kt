package com.danilkinkin.buckwheat.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.danilkinkin.buckwheat.di.DatabaseModule
import com.danilkinkin.buckwheat.entities.Draw
import com.danilkinkin.buckwheat.entities.Storage
import com.danilkinkin.buckwheat.utils.countDays
import com.danilkinkin.buckwheat.utils.isSameDay
import java.lang.Math.max
import java.util.*
import kotlin.math.abs
import kotlin.math.floor

class DrawsViewModel(application: Application) : AndroidViewModel(application) {
    enum class Stage { IDLE, CREATING_DRAW, EDIT_DRAW, COMMITTING_DRAW }
    enum class Action { PUT_NUMBER, SET_DOT, REMOVE_LAST }

    private val db = DatabaseModule.getInstance(application)

    private val draws = db.drawDao()
    private val storage = db.storageDao()

    var stage: MutableLiveData<Stage> = MutableLiveData(Stage.IDLE)
    var budgetOfCurrentDay: MutableLiveData<Double> = MutableLiveData(try {
        storage.get("currentDayBudget").value.toDouble()
    } catch (e: Exception) {
        0.0
    })
    var wholeBudget: MutableLiveData<Double> = MutableLiveData(try {
        storage.get("budget").value.toDouble()
    } catch (e: Exception) {
        0.0
    })
    var restBudget: MutableLiveData<Double> = MutableLiveData(try {
        storage.get("restBudget").value.toDouble()
    } catch (e: Exception) {
        0.0
    })
    var currentDraw: Double = 0.0

    var toDate: Date = try {
        Date(storage.get("budgetToDate").value.toLong())
    } catch (e: Exception) {
        Date()
    }
    var lastReCalcBudgetDate: Date? = try {
        Date(storage.get("lastReCalcBudgetDate").value.toLong())
    } catch (e: Exception) {
        null
    }

    var requireReCalcBudget: MutableLiveData<Boolean> = MutableLiveData(false)
    var requireSetBudget: MutableLiveData<Boolean> = MutableLiveData(false)

    var valueLeftDot: String = ""
    var valueRightDot: String = ""
    var useDot: Boolean = false

    init {
        if (lastReCalcBudgetDate !== null && !isSameDay(lastReCalcBudgetDate!!.time, Date().time)) {
            requireReCalcBudget.value = true
        }

        if (lastReCalcBudgetDate === null || toDate.time <= Date().time) {
            requireSetBudget.value = true
        }
    }

    fun getDraws(): LiveData<List<Draw>> {
        return draws.getAll()
    }

    fun changeBudget(budget: Double, toDate: Date) {
        storage.set(Storage("budget", budget.toString()))
        storage.set(Storage("budgetToDate", toDate.time.toString()))

        reCalcBudget(floor(budget / countDays(toDate)))

        wholeBudget.value = budget
        this.toDate = toDate
    }

    fun reCalcBudget(currentDayBudget: Double) {
        budgetOfCurrentDay.value = currentDayBudget
        restBudget.value = wholeBudget.value!! - currentDayBudget
        lastReCalcBudgetDate = Date()

        storage.set(Storage("currentDayBudget", currentDayBudget.toString()))
        storage.set(Storage("restBudget", restBudget.value.toString()))
        storage.set(Storage("lastReCalcBudgetDate", lastReCalcBudgetDate!!.time.toString()))
    }

    fun createDraw() {
        currentDraw = 0.0

        stage.value = Stage.CREATING_DRAW
    }

    fun editDraw(value: Double) {
        currentDraw = value

        stage.value = Stage.EDIT_DRAW
    }

    fun commitDraw() {
        if (stage.value !== Stage.EDIT_DRAW) return

        draws.insert(Draw(currentDraw, Date()))

        budgetOfCurrentDay.value = budgetOfCurrentDay.value?.minus(currentDraw)
        wholeBudget.value = wholeBudget.value?.minus(currentDraw)

        if (budgetOfCurrentDay.value!! < 0) {
            restBudget.value = wholeBudget.value!! - budgetOfCurrentDay.value!!.coerceAtLeast(0.0)
            storage.set(Storage("restBudget", restBudget.value.toString()))
        }

        storage.set(Storage("currentDayBudget", budgetOfCurrentDay.value.toString()))
        storage.set(Storage("budget", wholeBudget.value.toString()))

        currentDraw = 0.0
        valueLeftDot = ""
        valueRightDot = ""
        useDot = false

        stage.value = Stage.COMMITTING_DRAW
    }

    fun resetDraw() {
        currentDraw = 0.0
        valueLeftDot = ""
        valueRightDot = ""
        useDot = false

        stage.value = Stage.IDLE
    }

    fun removeDraw(draw: Draw) {
        draws.delete(draw)
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
                } else if (valueRightDot.length == 1) {
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

        editDraw("$valueLeftDot.$valueRightDot".toDouble())
    }
}
