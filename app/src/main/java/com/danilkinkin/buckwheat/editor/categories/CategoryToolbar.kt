package com.danilkinkin.buckwheat.editor.categories

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.base.CategoryChip
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.settings.CATEGORIES_SHEET

/**
 * One chip per category, plus a trailing "+" that opens the manager. Tapping the selected chip
 * clears it, which is how an expense becomes Uncategorised without a separate "none" chip.
 */
@Composable
fun CategoryToolbar(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
) {
    val categories by spendsViewModel.categories.observeAsState(emptyList())
    val selectedCategoryId by editorViewModel.currentCategoryId.observeAsState(null)

    // A category can be deleted from the manager while it is selected here; drop the dangling id
    // instead of saving a spend that points at nothing.
    LaunchedEffect(categories, selectedCategoryId) {
        val categoryId = selectedCategoryId
        if (categoryId != null && categories.none { it.id == categoryId }) {
            editorViewModel.currentCategoryId.value = null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
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
                .clickable {
                    appViewModel.openSheet(PathState(CATEGORIES_SHEET))
                },
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
}
