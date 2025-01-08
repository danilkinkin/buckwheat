package com.luna.dollargrain.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.base.TextFieldWithPaddings
import com.luna.dollargrain.data.ExtendCurrency
import com.luna.dollargrain.keyboard.KeyboardAction
import com.luna.dollargrain.keyboard.rememberAppKeyboard
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.util.combineColors
import com.luna.dollargrain.util.visualTransformationAsCurrency
import kotlinx.coroutines.awaitCancellation

@OptIn(ExperimentalComposeUiApi::class)
@Composable fun DisableSoftKeyboard(
    disable: Boolean = true,
    content: @Composable () -> Unit,
) {
    InterceptPlatformTextInput(
        interceptor = { request, nextHandler ->
            if (!disable) {
                nextHandler.startInputMethod(request)
            } else {
                awaitCancellation()
            }
        },
        content = content,
    )
}

@Composable
fun EditableTextWithLabel(
    modifier: Modifier = Modifier,
    value: String,
    currency: ExtendCurrency? = null,
    onChangeValue: (value: String) -> Unit = {},
    contentPaddingValues: PaddingValues = PaddingValues(start = 36.dp, end = 36.dp),
    focusRequester: FocusRequester = remember { FocusRequester() },
    showSystemKeyboard: Boolean = false
) {
    val context = LocalContext.current

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

    val textInputService = if (showSystemKeyboard) keyboardHandler else null

    Column(modifier) {
        CompositionLocalProvider(
            LocalTextInputService provides textInputService
        ) {
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (showSystemKeyboard) {
                    TextFieldWithPaddings(
                        value = value,
                        onChangeValue = { onChangeValue(it) },
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        visualTransformation = visualTransformationAsCurrency(
                            context,
                            currency = currency ?: ExtendCurrency.none(),
                            hintColor = color.copy(alpha = 0.2f),
                        ),
                        currency = currency,
                        focusRequester = focusRequester,
                        contentPadding = contentPaddingValues,
                    )
                } else {
                    DisableSoftKeyboard {
                        TextFieldWithPaddings(
                            value = value,
                            onChangeValue = { onChangeValue(it) },
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            visualTransformation = visualTransformationAsCurrency(
                                context,
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
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    DollargrainTheme {
        EditableTextWithLabel(
            value = "1 245 234 234 P",
        )
    }
}