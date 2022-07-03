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

    val db = DatabaseModule.getInstance(application)

    var stage: MutableLiveData<Stage> = MutableLiveData(Stage.IDLE)
    var budgetValue: Double = 10000.0
    var useDot: Boolean = false
    var drawValue: Double = 0.0

    init {

    }

    private val draws: MutableLiveData<List<Draw>> by lazy {
        MutableLiveData<List<Draw>>().also {
            loadDraws()
        }
    }

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
        budgetValue -= drawValue
        drawValue = 0.0

        db.drawDao().insertAll(Draw(drawValue, Date()))


        stage.value = Stage.COMMITTING_DRAW


        stage.value = Stage.IDLE
    }

    private fun loadDraws() {
        // Do an asynchronous operation to fetch users.
    }
}
