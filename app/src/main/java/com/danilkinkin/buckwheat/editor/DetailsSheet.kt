package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CategoryChip
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.settings.AddCategoryDialog
import com.danilkinkin.buckwheat.settings.nextCategoryColor
import androidx.compose.foundation.clickable

const val DETAILS_SHEET = "details"

/**
 * The two per-spend attributes in one place: one category and one free-form note.
 *
 * Keeping them together means the editor carries a single summary chip instead of two competing
 * chip rows, and the system keyboard only ever appears while this sheet is open.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsSheet(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val recentTags by spendsViewModel.tags.observeAsState(emptyList())
    val categories by spendsViewModel.categories.observeAsState(emptyList())
    val selectedCategoryId by editorViewModel.currentCategoryId.observeAsState(null)
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current

    var text by remember { mutableStateOf(editorViewModel.currentComment.value ?: "") }
    var showAddCategory by remember { mutableStateOf(false) }

    val navigationBarHeight = androidx.compose.ui.unit.max(
        LocalWindowInsets.current.calculateBottomPadding(),
        16.dp,
    )

    fun updateNote(value: String) {
        text = value
        // Live-update the editor's note; the spend itself is only written on commit.
        editorViewModel.currentComment.value = value
    }

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
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
                    text = stringResource(R.string.details),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            Text(
                text = stringResource(R.string.categories_title),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CategoryChip(
                    text = stringResource(R.string.no_category),
                    selected = selectedCategoryId == null,
                    onClick = { editorViewModel.currentCategoryId.value = null },
                )
                categories.forEach { category ->
                    CategoryChip(
                        text = category.name,
                        emoji = category.emoji,
                        selected = category.id == selectedCategoryId,
                        onClick = {
                            editorViewModel.currentCategoryId.value =
                                if (category.id == selectedCategoryId) null else category.id
                        },
                    )
                }
                Surface(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { showAddCategory = true },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Text(
                        text = "+",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { updateNote(it) },
                placeholder = {
                    Text(stringResource(R.string.add_note))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )

            if (recentTags.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.recent),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    recentTags.take(8).forEach { tag ->
                        CategoryChip(
                            text = tag,
                            selected = false,
                            onClick = { updateNote(tag) },
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

    if (showAddCategory) {
        AddCategoryDialog(
            nextColor = nextCategoryColor(categories.size),
            onAdd = { name, emoji, color ->
                spendsViewModel.addCategory(name, emoji, color)
                showAddCategory = false
            },
            onClose = { showAddCategory = false },
        )
    }
}
