package com.danilkinkin.buckwheat.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.danilkinkin.buckwheat.R

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

@Composable
fun AddCategoryDialog(
    nextColor: Long,
    onAdd: (name: String, emoji: String, color: Long) -> Unit,
    onClose: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.add_category),
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
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { value ->
                        // Keep one grapheme-ish: most emoji are one or two UTF-16 units.
                        emoji = value.take(2)
                    },
                    label = { Text(stringResource(R.string.category_emoji)) },
                    singleLine = true,
                    modifier = Modifier.width(120.dp),
                )
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
                        onClick = { onAdd(name, emoji, nextColor) },
                        enabled = name.isNotBlank(),
                    ) {
                        Text(stringResource(R.string.add))
                    }
                }
            }
        }
    }
}
