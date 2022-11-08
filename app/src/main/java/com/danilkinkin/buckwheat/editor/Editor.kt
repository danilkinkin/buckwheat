package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

enum class AnimState { EDITING, COMMIT, IDLE, RESET }

@Composable
fun Editor(
    modifier: Modifier = Modifier,
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onOpenHistory: (() -> Unit)? = null,
) {
    val focusController = remember { FocusController() }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxHeight()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { focusController.focus() }
        ) {
            EditorToolbar(onOpenHistory = onOpenHistory)
            RestBudget()
            CurrentSpendEditor(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                focusController = focusController,
            )
            TaggingSpent()
            Spacer(Modifier.height(24.dp))
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