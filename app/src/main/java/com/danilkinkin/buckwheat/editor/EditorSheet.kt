package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CategoryChip
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SubscriptionsViewModel
import com.danilkinkin.buckwheat.editor.dateTimeEdit.DateTimeEditPill
import com.danilkinkin.buckwheat.settings.AddCategoryDialog
import com.danilkinkin.buckwheat.settings.nextCategoryColor
import com.danilkinkin.buckwheat.subscriptions.SubscriptionForm
import com.danilkinkin.buckwheat.ui.colorOnEditor

const val EDITOR_SHEET = "editor"

private enum class AddTab { EXPENSE, SUBSCRIPTION }

/**
 * One add popup with two tabs. Expense is the default; both tabs share the same card language but
 * use a different container tone (primary vs tertiary) so they read as siblings, not clones.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorSheet(
    initialTab: String = "expense",
    onClose: () -> Unit,
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    subscriptionsViewModel: SubscriptionsViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
) {
    val mode by editorViewModel.mode.observeAsState(EditMode.ADD)
    val categories by spendsViewModel.categories.observeAsState(emptyList())
    val selectedCategoryId by editorViewModel.currentCategoryId.observeAsState(null)
    val comment by editorViewModel.currentComment.observeAsState("")

    var tab by remember {
        mutableStateOf(
            if (initialTab == "subscription") AddTab.SUBSCRIPTION else AddTab.EXPENSE
        )
    }
    var showAddCategory by remember { mutableStateOf(false) }
    val focusController = remember { FocusController() }

    fun cancel() {
        editorViewModel.resetEditingSpent()
        onClose()
    }

    fun saveExpense() {
        if (!editorViewModel.canCommitEditingSpent()) return
        commitSpent(editorViewModel, spendsViewModel)
        onClose()
    }

    DisposableEffect(Unit) {
        onDispose { editorViewModel.resetEditingSpent() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
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
                text = if (tab == AddTab.SUBSCRIPTION) {
                    stringResource(R.string.add_subscription)
                } else {
                    stringResource(
                        if (mode == EditMode.EDIT) R.string.edit_expense else R.string.new_expense
                    )
                },
                style = MaterialTheme.typography.titleMedium,
                color = colorOnEditor,
                modifier = Modifier.weight(1f),
            )
            if (tab == AddTab.EXPENSE) {
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
                IconButton(onClick = { saveExpense() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_apply),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        AddTabs(
            selected = tab,
            onSelect = {
                if (it == AddTab.EXPENSE) {
                    tab = AddTab.EXPENSE
                } else {
                    editorViewModel.resetEditingSpent()
                    tab = AddTab.SUBSCRIPTION
                }
            },
        )

        Spacer(Modifier.height(12.dp))

        if (tab == AddTab.EXPENSE) {
            // Primary tone: the everyday action.
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(20.dp)) {
                    if (mode == EditMode.EDIT) {
                        DateTimeEditPill()
                    }

                    CurrentSpendEditor(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        focusController = focusController,
                        onDone = { saveExpense() },
                    )

                    Spacer(Modifier.height(12.dp))

                    FlowRow(
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

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { editorViewModel.currentComment.value = it },
                        label = { Text(stringResource(R.string.note)) },
                        placeholder = { Text(stringResource(R.string.add_note)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } else {
            // Tertiary tone: recurring money, visually a sibling of the expense card.
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.22f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                SubscriptionForm(
                    initial = null,
                    categories = categories,
                    onSave = { name, amount, period, nextDue, categoryId ->
                        subscriptionsViewModel.add(name, amount, period, nextDue, categoryId)
                        onClose()
                    },
                    modifier = Modifier.padding(20.dp),
                )
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

@Composable
private fun AddTabs(
    selected: AddTab,
    onSelect: (AddTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AddTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val container = when {
                isSelected && tab == AddTab.EXPENSE -> MaterialTheme.colorScheme.primaryContainer
                isSelected -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val content = when {
                isSelected && tab == AddTab.EXPENSE -> MaterialTheme.colorScheme.onPrimaryContainer
                isSelected -> MaterialTheme.colorScheme.onTertiaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .clickable { onSelect(tab) },
                shape = CircleShape,
                color = container,
                contentColor = content,
            ) {
                Text(
                    text = if (tab == AddTab.EXPENSE) {
                        stringResource(R.string.expense)
                    } else {
                        stringResource(R.string.subscriptions_title)
                    },
                    modifier = Modifier.padding(vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}
