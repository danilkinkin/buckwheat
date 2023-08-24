package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.base.TextFieldWithPaddings
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.keyboard.KeyboardAction
import com.danilkinkin.buckwheat.keyboard.rememberAppKeyboard
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*

@Composable
fun EditableTextWithLabel(
    modifier: Modifier = Modifier,
    value: String,
    currency: ExtendCurrency? = null,
    onChangeValue: (value: String) -> Unit = {},
    contentPaddingValues: PaddingValues = PaddingValues(start = 36.dp, end = 36.dp),
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val color = contentColorFor(
        combineColors(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surfaceVariant,
            angle = 0.9F,
        )
    )

    val keyboardHandler = rememberAppKeyboard(manualDispatcher = { action, _ ->
        if (action == KeyboardAction.REMOVE_LAST && value == "") {
            onChangeValue("")
        }
    })

    Column(modifier) {
        CompositionLocalProvider(
            LocalTextInputService provides keyboardHandler
        ) {
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                TextFieldWithPaddings(
                    value = value,
                    onChangeValue = { onChangeValue(it) },
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    visualTransformation = visualTransformationAsCurrency(
                        currency = currency ?: ExtendCurrency.none(),
                        hintColor = color.copy(alpha = 0.2f),
                    ),
                    currency = currency,
                    focusRequester = focusRequester,
                    contentPadding = contentPaddingValues,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        EditableTextWithLabel(
            value = "1 245 234 234 P",
        )
    }
}