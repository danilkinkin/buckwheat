package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CategoryDropdown
import com.danilkinkin.buckwheat.base.MinimalTextField
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
 * Minimal add card. No titles, no tinted panels: a two-part segmented control, the amount, one
 * category pill, one note field, one Save button. The subscription tab uses the same skeleton.
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    var defaultCategoryApplied by remember { mutableStateOf(false) }
    LaunchedEffect(categories) {
        if (!defaultCategoryApplied && categories.isNotEmpty()) {
            defaultCategoryApplied = true

            if (mode == EditMode.ADD && editorViewModel.currentCategoryId.value == null) {
                editorViewModel.currentCategoryId.value =
                    categories.firstOrNull { it.name.equals("Other", ignoreCase = true) }?.id
            }
        }
    }

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
            .padding(bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { cancel() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = null,
                    tint = colorOnEditor,
                )
            }

            SingleChoiceSegmentedButtonRow(Modifier.weight(1f)) {
                SegmentedButton(
                    selected = tab == AddTab.EXPENSE,
                    onClick = {
                        tab = AddTab.EXPENSE
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    label = { Text(stringResource(R.string.expense)) },
                )
                SegmentedButton(
                    selected = tab == AddTab.SUBSCRIPTION,
                    onClick = {
                        editorViewModel.resetEditingSpent()
                        tab = AddTab.SUBSCRIPTION
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    label = { Text(stringResource(R.string.subscriptions_title)) },
                )
            }

            if (tab == AddTab.EXPENSE && mode == EditMode.EDIT) {
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
            } else {
                Spacer(Modifier.width(48.dp))
            }
        }

        if (tab == AddTab.EXPENSE) {
            if (mode == EditMode.EDIT) {
                DateTimeEditPill()
            }

            CurrentSpendEditor(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                focusController = focusController,
                onDone = { saveExpense() },
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryDropdown(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onSelect = { editorViewModel.currentCategoryId.value = it },
                    onAddCategory = { showAddCategory = true },
                )
            }

            MinimalTextField(
                value = comment,
                onValueChange = { editorViewModel.currentComment.value = it },
                placeholder = stringResource(R.string.add_note),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp),
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { saveExpense() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(26.dp),
            ) {
                Text(stringResource(R.string.save))
            }
        } else {
            SubscriptionForm(
                initial = null,
                categories = categories,
                onSave = { name, amount, period, nextDue, categoryId ->
                    subscriptionsViewModel.add(name, amount, period, nextDue, categoryId)
                    onClose()
                },
                modifier = Modifier.padding(horizontal = 24.dp),
                onAddCategory = { showAddCategory = true },
            )
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
