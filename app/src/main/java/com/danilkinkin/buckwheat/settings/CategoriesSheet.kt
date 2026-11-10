package com.danilkinkin.buckwheat.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.CategoryIcon
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.entities.Category

const val CATEGORIES_SHEET = "categories"

@Composable
fun CategoriesSheet(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val categories by spendsViewModel.categories.observeAsState(emptyList())
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current
    var showAddDialog by remember { mutableStateOf(false) }
    val removedMessage = stringResource(R.string.remove_category)
    val undoLabel = stringResource(R.string.remove_spent_undo)

    val navigationBarHeight = androidx.compose.ui.unit.max(
        LocalWindowInsets.current.calculateBottomPadding(),
        16.dp,
    )

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
                    text = stringResource(R.string.categories_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            if (categories.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_categories),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                ) {
                    items(categories, key = { it.id }) { category ->
                        CategoryRow(
                            category = category,
                            onDelete = {
                                spendsViewModel.deleteCategory(category) { affected ->
                                    appViewModel.showSnackbar(
                                        message = removedMessage,
                                        actionLabel = undoLabel,
                                        duration = SnackbarDuration.Long,
                                    ) { result ->
                                        if (result == SnackbarResult.ActionPerformed) {
                                            spendsViewModel.restoreCategory(category, affected)
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }

            ButtonRow(
                icon = painterResource(R.drawable.ic_label),
                text = stringResource(R.string.add_category),
                onClick = { showAddDialog = true },
            )

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            nextColor = nextCategoryColor(categories.size),
            onAdd = { name, icon, color ->
                spendsViewModel.addCategory(name, icon, color)
                showAddDialog = false
            },
            onClose = { showAddDialog = false },
        )
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(
            category = category,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete) {
            Icon(
                painter = painterResource(R.drawable.ic_delete_forever),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}
