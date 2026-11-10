package com.danilkinkin.buckwheat.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.categoryIconKeys
import com.danilkinkin.buckwheat.base.categoryIconRes
import com.danilkinkin.buckwheat.data.entities.Category

private val categoryPalette = listOf(
    0xFFFF8A65, // deep orange
    0xFF4FC3F7, // light blue
    0xFF81C784, // green
    0xFFBA68C8, // purple
    0xFFE57373, // red
    0xFF90A4AE, // blue grey
    0xFFFFD54F, // amber
    0xFF4DB6AC, // teal
)

/** Deterministic colour for a new category, based on how many already exist. */
fun nextCategoryColor(categoryCount: Int): Long =
    categoryPalette[categoryCount % categoryPalette.size]

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorDialog(
    nextColor: Long,
    onSave: (name: String, icon: String, color: Long) -> Unit,
    onClose: () -> Unit,
    initial: Category? = null,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var icon by remember { mutableStateOf(initial?.icon ?: "other") }
    var color by remember { mutableLongStateOf(initial?.color ?: nextColor) }
    val editing = initial != null

    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(
                        if (editing) R.string.edit_category else R.string.add_category
                    ),
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.category_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.category_icon),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categoryIconKeys.forEach { key ->
                        val selected = key == icon

                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .then(
                                    if (selected) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape,
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { icon = key },
                            shape = CircleShape,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            },
                            contentColor = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(categoryIconRes(key)!!),
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.category_color),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    categoryPalette.forEach { swatch ->
                        val selected = swatch == color

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(swatch))
                                .then(
                                    if (selected) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape,
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .clickable { color = swatch },
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onClose,
                        colors = ButtonDefaults.textButtonColors(),
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(name, icon, color) },
                        enabled = name.isNotBlank(),
                    ) {
                        Text(stringResource(if (editing) R.string.save else R.string.add))
                    }
                }
            }
        }
    }
}
