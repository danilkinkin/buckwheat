package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.entities.Category

/**
 * A compact category pill that opens a popup of wrapping pills, like MediaTracker's status
 * selector. Big targets, no vertical list, no anchored dropdown.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryPicker(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    onAddCategory: (() -> Unit)? = null,
) {
    var open by remember { mutableStateOf(false) }
    val selected = categories.firstOrNull { it.id == selectedCategoryId }

    Box(modifier) {
        Surface(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { open = true },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selected != null) {
                    CategoryIcon(
                        category = selected,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = selected?.name ?: stringResource(R.string.no_category),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_down),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }

    if (open) {
        Dialog(onDismissRequest = { open = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.widthIn(min = 300.dp, max = 400.dp),
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.category),
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Spacer(Modifier.height(16.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                    ) {
                        CategoryChip(
                            text = stringResource(R.string.no_category),
                            selected = selectedCategoryId == null,
                            onClick = {
                                onSelect(null)
                                open = false
                            },
                        )

                        categories.forEach { category ->
                            CategoryChip(
                                text = category.name,
                                iconKey = category.icon,
                                selected = category.id == selectedCategoryId,
                                onClick = {
                                    onSelect(category.id)
                                    open = false
                                },
                            )
                        }

                        if (onAddCategory != null) {
                            CategoryChip(
                                text = "+ ${stringResource(R.string.add_category)}",
                                selected = false,
                                onClick = {
                                    open = false
                                    onAddCategory()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
