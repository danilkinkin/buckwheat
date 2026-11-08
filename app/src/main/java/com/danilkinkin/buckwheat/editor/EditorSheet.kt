package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.dateTimeEdit.DateTimeEditPill
import com.danilkinkin.buckwheat.ui.colorOnEditor

const val EDITOR_SHEET = "editor"

/**
 * The expense editor as a popup sheet: amount and category/note only, with the system numeric
 * keyboard. It is a regular sheet in the activity window so the Details sheet can stack above it.
 */
@Composable
fun EditorSheet(
    onClose: () -> Unit,
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
) {
    val mode by editorViewModel.mode.observeAsState(EditMode.ADD)
    val focusController = remember { FocusController() }

    fun cancel() {
        editorViewModel.resetEditingSpent()
        onClose()
    }

    fun save() {
        if (!editorViewModel.canCommitEditingSpent()) return

        commitSpent(editorViewModel, spendsViewModel)
        onClose()
    }

    // Covers dismissing the sheet by swipe/scrim too, not only the X button.
    DisposableEffect(Unit) {
        onDispose { editorViewModel.resetEditingSpent() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { cancel() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = null,
                    tint = colorOnEditor,
                )
            }
            Text(
                text = stringResource(
                    if (mode == EditMode.EDIT) R.string.edit_expense else R.string.new_expense
                ),
                style = MaterialTheme.typography.titleMedium,
                color = colorOnEditor,
                modifier = Modifier.weight(1f),
            )
            if (mode == EditMode.EDIT) {
                IconButton(
                    onClick = {
                        editorViewModel.editedTransaction?.let { spendsViewModel.removeSpent(it) }
                        cancel()
                    },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_forever),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            IconButton(onClick = { save() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_apply),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        if (mode == EditMode.EDIT) {
            DateTimeEditPill()
        }

        DetailsToolbar()

        CurrentSpendEditor(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            focusController = focusController,
            onDone = { save() },
        )

        Spacer(Modifier.height(8.dp))
    }
}
