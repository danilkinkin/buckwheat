package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CategoryChip
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import kotlinx.coroutines.delay

const val NOTE_SHEET = "note"

/**
 * Tags are free-form notes on a single expense; categories are the one structured
 * classification. Keeping the note editor in its own sheet means the editor carries one chip row
 * instead of two competing ones, and the system keyboard never has to fight the keypad layout.
 */
@Composable
fun NoteSheet(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val recentTags by spendsViewModel.tags.observeAsState(emptyList())
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current

    var text by remember { mutableStateOf(editorViewModel.currentComment.value ?: "") }

    val focusRequester = remember { FocusRequester() }
    val navigationBarHeight = androidx.compose.ui.unit.max(
        LocalWindowInsets.current.calculateBottomPadding(),
        16.dp,
    )

    fun update(value: String) {
        text = value
        // Live-update the editor's note; the spend itself is only written on commit.
        editorViewModel.currentComment.value = value
    }

    androidx.compose.material3.Surface(
        Modifier.padding(top = localBottomSheetScrollState.topPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(bottom = navigationBarHeight),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.note),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            OutlinedTextField(
                value = text,
                onValueChange = { update(it) },
                placeholder = {
                    Text(stringResource(R.string.add_note))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .focusRequester(focusRequester),
            )

            if (recentTags.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.recent),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    recentTags.take(8).forEach { tag ->
                        CategoryChip(
                            text = tag,
                            selected = false,
                            onClick = { update(tag) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onClose) {
                    Text(stringResource(R.string.accept))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        // The bottom-sheet wrapper clears focus when it appears; wait for that to settle.
        delay(180)
        focusRequester.requestFocus()
    }
}
