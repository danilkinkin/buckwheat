package com.danilkinkin.buckwheat.editor.tagging

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.EditStage
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.editor.FocusController
import com.danilkinkin.buckwheat.util.observeLiveData

@Composable
fun TaggingToolbar(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
    editorFocusController: FocusController
) {
    val tags by spendsViewModel.tags.observeAsState(emptyList())
    val currentComment by editorViewModel.currentComment.observeAsState("")

    var showAddComment by remember { mutableStateOf(false) }
    var isEdit by remember { mutableStateOf(false) }

    observeLiveData(editorViewModel.stage) {
        showAddComment = it === EditStage.EDIT_SPENT
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width = maxWidth - 48.dp

        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(44.dp)
                .horizontalScroll(
                    state = rememberScrollState(),
                    enabled = !isEdit,
                    reverseScrolling = true,
                )
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            // Tags used to sit flush against each other; 8dp keeps them distinct while still
            // leaving the wider gap before the tag-editor chip.
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            // No entrance animation: fighting the IME and sheet transitions with per-chip
            // fades was what made this row feel laggy.
            if (showAddComment) {
                tags.take(5).reversed().filter { it != currentComment }.forEach { tag ->
                    Tag(value = tag, onClick = {
                        editorViewModel.currentComment.value = tag
                    })
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (showAddComment) {
                CustomTag(
                    onlyIcon = tags.isNotEmpty(),
                    editorFocusController = editorFocusController,
                    extendWidth = width,
                    onEdit = { isEdit = it },
                )
            }
        }
    }
}