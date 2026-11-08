package com.danilkinkin.buckwheat.editor

import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import java.util.Date

/**
 * Single commit path for the editor, used by the toolbar check and the IME's Done action.
 * Editing removes the old row and inserts the updated one, exactly as the old keypad did.
 */
fun commitSpent(
    editorViewModel: EditorViewModel,
    spendsViewModel: SpendsViewModel,
) {
    if (!editorViewModel.canCommitEditingSpent()) return

    if (editorViewModel.mode.value == EditMode.EDIT) {
        val edited = editorViewModel.editedTransaction ?: return

        spendsViewModel.removeSpent(edited, silent = true)
        spendsViewModel.addSpent(
            edited.copy(
                value = editorViewModel.currentSpent,
                date = editorViewModel.currentDate,
                comment = (editorViewModel.currentComment.value ?: "").trim(),
                categoryId = editorViewModel.currentCategoryId.value,
            )
        )
    } else {
        spendsViewModel.addSpent(
            Transaction(
                type = TransactionType.SPENT,
                value = editorViewModel.currentSpent,
                date = Date(),
                comment = (editorViewModel.currentComment.value ?: "").trim(),
                categoryId = editorViewModel.currentCategoryId.value,
            )
        )
    }

    editorViewModel.resetEditingSpent()
}
