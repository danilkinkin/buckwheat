package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.TextFieldWithPaddings
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.keyboard.rememberAppKeyboard
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*

@Composable
fun EditableTextWithLabel(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    placeholder: String = "",
    currency: ExtendCurrency? = null,
    onChangeValue: (value: String) -> Unit = {},
    fontSizeValue: TextUnit = MaterialTheme.typography.displayLarge.fontSize,
    fontSizeLabel: TextUnit = MaterialTheme.typography.labelMedium.fontSize,
    contentPaddingValues: PaddingValues = PaddingValues(start = 36.dp, end = 36.dp),
    focusRequester: FocusRequester = remember { FocusRequester() },
    placeholderStyle: SpanStyle = SpanStyle(),
) {
    val color = contentColorFor(
        combineColors(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surfaceVariant,
            angle = 0.9F,
        )
    )

    val keyboardHandler = rememberAppKeyboard(manualDispatcher = { action, _ ->
        if (action == SpendsViewModel.Action.REMOVE_LAST && value == "") {
            onChangeValue("")
        }
    })

    Column(modifier) {
        CompositionLocalProvider(
            LocalTextInputService provides keyboardHandler
        ) {
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterStart,
            ) {
                TextFieldWithPaddings(
                    value = value,
                    onChangeValue = { onChangeValue(it) },
                    textStyle = MaterialTheme.typography.displayLarge.copy(
                        fontSize = fontSizeValue,
                        color = color,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    visualTransformation = visualTransformationAsCurrency(
                        currency = currency ?: ExtendCurrency(type = CurrencyType.NONE),
                        hintColor = color.copy(alpha = 0.2f),
                        placeholder = placeholder,
                        placeholderStyle = placeholderStyle,
                    ),
                    focusRequester = focusRequester,
                )
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = fontSizeLabel,
            color = color.copy(alpha = 0.7f),
            overflow = TextOverflow.Ellipsis,
            softWrap = false,
            modifier = Modifier.padding(contentPaddingValues),
        )
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        EditableTextWithLabel(
            value = "1 245 234 234 P",
            label = stringResource(id = R.string.budget_for_today),
        )
    }
}