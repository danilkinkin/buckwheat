package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.entities.Category

/**
 * One compact category selector. Categories are a dropdown, not a wall of chips: the current
 * choice stays visible and opening the menu is the single extra tap.
 */
@Composable
fun CategoryDropdown(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    onAddCategory: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = categories.firstOrNull { it.id == selectedCategoryId }

    Box(modifier) {
        Surface(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { expanded = true },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!selected?.emoji.isNullOrEmpty()) {
                    Text(
                        text = selected.emoji,
                        style = MaterialTheme.typography.bodyMedium,
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.no_category)) },
                onClick = {
                    onSelect(null)
                    expanded = false
                },
                trailingIcon = {
                    if (selectedCategoryId == null) {
                        Icon(
                            painter = painterResource(R.drawable.ic_apply),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )

            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (category.emoji.isNotEmpty()) {
                                Text(category.emoji)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(category.name)
                        }
                    },
                    onClick = {
                        onSelect(category.id)
                        expanded = false
                    },
                    trailingIcon = {
                        if (category.id == selectedCategoryId) {
                            Icon(
                                painter = painterResource(R.drawable.ic_apply),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
            }

            if (onAddCategory != null) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.add_category)) },
                    onClick = {
                        expanded = false
                        onAddCategory()
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = null,
                        )
                    },
                )
            }
        }
    }
}
