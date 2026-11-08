package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/** Borderless soft field used by the minimal add/edit cards. */
@Composable
fun MinimalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    containerColor: Color? = null,
) {
    // Data entry should carry the palette, not read as a grey box.
    val container = containerColor ?: MaterialTheme.colorScheme.primaryContainer
    val focusedContainer = container.copy(alpha = 0.55f)
    val unfocusedContainer = container.copy(alpha = 0.38f)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = focusedContainer,
            unfocusedContainerColor = unfocusedContainer,
            disabledContainerColor = unfocusedContainer,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
        ),
    )
}
