package com.luna.dollargrain.editor

import androidx.lifecycle.*
import com.luna.dollargrain.data.entities.Transaction
import com.luna.dollargrain.util.join
import com.luna.dollargrain.util.tryConvertStringToNumber
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

    var editedTransaction: Transaction? = null
    var currentDate: Date = Date()
    var currentSpent: BigDecimal = BigDecimal.ZERO
    var currentComment = MutableLiveData("")
    var rawSpentValue = MutableLiveData("")

    fun startEditingSpent(transaction: Transaction) {
        editedTransaction = transaction
        currentSpent = transaction.value
        currentDate = transaction.date
        currentComment.value = transaction.comment
        rawSpentValue.value = tryConvertStringToNumber(transaction.value.toString()).join(third = false)

        stage.value = EditStage.EDIT_SPENT
        mode.value = EditMode.EDIT
    }

    fun startCreatingSpent() {
        currentSpent = BigDecimal.ZERO

        stage.value = EditStage.CREATING_SPENT
    }

    fun modifyEditingSpent(value: BigDecimal) {
        currentSpent = value

        stage.value = EditStage.EDIT_SPENT
    }

    fun resetEditingSpent() {
        currentSpent = BigDecimal.ZERO
        currentDate = Date()
        currentComment.value = ""
        rawSpentValue.value = ""

        stage.value = EditStage.IDLE
        mode.value = EditMode.ADD
        editedTransaction = null
    }

    fun canCommitEditingSpent(): Boolean {
        if (stage.value !== EditStage.EDIT_SPENT) return false

        val formatSpent = currentSpent
            .setScale(2, RoundingMode.HALF_EVEN)
            .stripTrailingZeros()
            .toPlainString()

        if (formatSpent == "0") return false

        return true
    }
}