package com.danilkinkin.buckwheat.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.danilkinkin.buckwheat.di.DatabaseModule
import com.danilkinkin.buckwheat.entities.Draw
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class DrawsViewModel(application: Application) : AndroidViewModel(application) {
    enum class Stage { IDLE, CREATING_DRAW, EDIT_DRAW, COMMITTING_DRAW }
    enum class Action { PUT_NUMBER, SET_DOT, REMOVE_LAST }

    private val db = DatabaseModule.getInstance(application)

    var stage: MutableLiveData<Stage> = MutableLiveData(Stage.IDLE)
    var budgetValue: Double = 10000.0
    var drawValue: Double = 0.0

    var valueLeftDot: String = ""
    var valueRightDot: String = ""
    var useDot: Boolean = false

    init {

    }

    private val draws = db.drawDao().getAll()

    fun getDraws(): LiveData<List<Draw>> {
        return draws
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

        db.drawDao().insertAll(Draw(drawValue, Date()))

        budgetValue -= drawValue
        drawValue = 0.0

        stage.value = Stage.COMMITTING_DRAW


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
            }
        }

        editDraw("$valueLeftDot.$valueRightDot".toDouble())
    }
}
