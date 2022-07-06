package com.danilkinkin.buckwheat.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.danilkinkin.buckwheat.di.DatabaseModule
import com.danilkinkin.buckwheat.entities.Draw
import com.danilkinkin.buckwheat.entities.Storage
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class DrawsViewModel(application: Application) : AndroidViewModel(application) {
    enum class Stage { IDLE, CREATING_DRAW, EDIT_DRAW, COMMITTING_DRAW }
    enum class Action { PUT_NUMBER, SET_DOT, REMOVE_LAST }

    private val db = DatabaseModule.getInstance(application)

    private val draws = db.drawDao()
    private val storage = db.storageDao()

    var stage: MutableLiveData<Stage> = MutableLiveData(Stage.IDLE)
    var budgetValue: MutableLiveData<Double> = MutableLiveData(try {
        storage.get("budget").value.toDouble()
    } catch (e: Exception) {
        0.0
    })
    var drawValue: Double = 0.0

    var toDate: Date = try {
        Date(storage.get("toDate").value.toLong())
    } catch (e: Exception) {
        Date()
    }

    var valueLeftDot: String = ""
    var valueRightDot: String = ""
    var useDot: Boolean = false

    init {

    }

    fun getDraws(): LiveData<List<Draw>> {
        return draws.getAll()
    }

    fun changeBudget(budget: Double, toDate: Date) {
        storage.set(Storage("budget", budget.toString()))
        storage.set(Storage("toDate", toDate.time.toString()))

        budgetValue.value = budget
    }

    fun createDraw() {
        drawValue = 0.0

        stage.value = Stage.CREATING_DRAW
    }

    fun editDraw(value: Double) {
        drawValue = value

        stage.value = Stage.EDIT_DRAW
    }

    fun commitDraw() {
        if (stage.value !== Stage.EDIT_DRAW) return

        draws.insert(Draw(drawValue, Date()))

        budgetValue.value = budgetValue.value?.minus(drawValue)

        storage.set(Storage("budget", budgetValue.value.toString()))

        drawValue = 0.0
        valueLeftDot = ""
        valueRightDot = ""
        useDot = false

        stage.value = Stage.COMMITTING_DRAW


        stage.value = Stage.IDLE
    }

    fun resetDraw() {
        drawValue = 0.0
        valueLeftDot = ""
        valueRightDot = ""
        useDot = false

        stage.value = Stage.IDLE
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
