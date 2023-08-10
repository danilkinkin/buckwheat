package com.danilkinkin.buckwheat.editor

import androidx.lifecycle.*
import com.danilkinkin.buckwheat.data.entities.Spent
import com.danilkinkin.buckwheat.util.join
import com.danilkinkin.buckwheat.util.tryConvertStringToNumber
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import javax.inject.Inject

enum class EditMode { ADD, EDIT }
enum class EditStage { IDLE, CREATING_SPENT, EDIT_SPENT, COMMITTING_SPENT }

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    var mode = MutableLiveData(EditMode.ADD)
    var stage = MutableLiveData(EditStage.IDLE)

    var editedSpent: Spent? = null
    var currentDate: Date = Date()
    var currentSpent: BigDecimal = 0.0.toBigDecimal()
    var currentComment: String = ""
    var rawSpentValue = MutableLiveData("")

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

    fun canCommitEditingSpent(): Boolean {
        if (stage.value !== EditStage.EDIT_SPENT) return false

        val formatSpent = currentSpent
            .setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()

        if (formatSpent == "0") return false

        return true
    }
}