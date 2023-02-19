package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

@Composable
fun CheckedRow(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onValueChange: (isChecked: Boolean) -> Unit,
    text: String,
    endContent: @Composable (() -> Unit)? = null,
) {
    TextRow(
        modifier = modifier
            .toggleable(
                value = checked,
                onValueChange = { onValueChange(!checked) },
                role = Role.Checkbox
            ),
        icon = if (checked) painterResource(R.drawable.ic_apply) else null,
        iconTint = MaterialTheme.colorScheme.primary,
        text = text,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        endContent = endContent,
    )
}

@Preview
@Composable
fun PreviewCheckedRow() {
    val (checkedState, onStateChange) = remember { mutableStateOf(false) }

    BuckwheatTheme {
        CheckedRow(
            checked = checkedState,
            onValueChange = { onStateChange(it) },
            text = "Option selection",
        )
    }
}

@Preview
@Composable
fun PreviewCheckedRowChekecd() {
    val (checkedState, onStateChange) = remember { mutableStateOf(true) }

    BuckwheatTheme {
        CheckedRow(
            checked = checkedState,
            onValueChange = { onStateChange(it) },
            text = "Option selection",
        )
    }
}