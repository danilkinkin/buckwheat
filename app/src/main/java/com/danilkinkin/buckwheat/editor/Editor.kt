package com.danilkinkin.buckwheat.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.tagging.TaggingToolbar
import com.danilkinkin.buckwheat.editor.dateTimeEdit.DateTimeEditPill
import com.danilkinkin.buckwheat.editor.toolbar.EditorToolbar
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

enum class AnimState { EDITING, COMMIT, IDLE, RESET }

@Composable
fun Editor(
    modifier: Modifier = Modifier,
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
    onOpenHistory: (() -> Unit)? = null,
) {
    val focusController = remember { FocusController() }
    val mode by editorViewModel.mode.observeAsState(EditMode.ADD)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxHeight()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusController.focus() }
        ) {
            EditorToolbar()
            if (mode == EditMode.EDIT) {
                DateTimeEditPill()
            }
            CurrentSpendEditor(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                focusController = focusController,
            )
            TaggingToolbar(editorFocusController = focusController)
            Spacer(Modifier.height(24.dp))

            BackHandler(mode == EditMode.EDIT) {
                editorViewModel.resetEditingSpent()
            }
        }
    }
}

@Preview
@Composable
fun EditorPreview() {
    BuckwheatTheme {
        Editor()
    }
}
