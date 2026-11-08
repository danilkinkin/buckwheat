package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
 * A compact category pill that opens a real popup, not an anchored dropdown. Bigger touch
 * targets, a title, and the list is easy to scan on a phone.
 */
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
                if (!selected?.emoji.isNullOrEmpty()) {
                    Text(selected.emoji, style = MaterialTheme.typography.bodyMedium)
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }

    if (open) {
        Dialog(onDismissRequest = { open = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.widthIn(min = 280.dp, max = 380.dp),
            ) {
                Column(Modifier.padding(vertical = 12.dp)) {
                    Text(
                        text = stringResource(R.string.category),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    )

                    LazyColumn(Modifier.heightIn(max = 420.dp)) {
                        item("none") {
                            PickerRow(
                                emoji = null,
                                text = stringResource(R.string.no_category),
                                selected = selectedCategoryId == null,
                                onClick = {
                                    onSelect(null)
                                    open = false
                                },
                            )
                        }

                        items(categories, key = { it.id }) { category ->
                            PickerRow(
                                emoji = category.emoji,
                                text = category.name,
                                selected = category.id == selectedCategoryId,
                                onClick = {
                                    onSelect(category.id)
                                    open = false
                                },
                            )
                        }
                    }

                    if (onAddCategory != null) {
                        HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    open = false
                                    onAddCategory()
                                }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.add_category),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerRow(
    emoji: String?,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!emoji.isNullOrEmpty()) {
            Text(emoji, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.width(12.dp))
        } else {
            Spacer(Modifier.width(28.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_apply),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
