package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.keyboard.rememberAppKeyboard
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.*


@Composable
fun EditorRow(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    editable: Boolean = false,
    currency: ExtendCurrency? = null,
    onChangeValue: (value: String) -> Unit = {},
    fontSizeValue: TextUnit = MaterialTheme.typography.displayLarge.fontSize,
    fontSizeLabel: TextUnit = MaterialTheme.typography.labelMedium.fontSize,
    contentPaddingValues: PaddingValues = PaddingValues(start = 36.dp, end = 36.dp)
) {
    val color = contentColorFor(
        combineColors(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surfaceVariant,
            angle = 0.9F,
        )
    )

    Column(
        modifier = modifier
    ) {
        if (editable) {
            CompositionLocalProvider(
                LocalTextInputService provides rememberAppKeyboard()
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = {
                        val converted = tryConvertStringToNumber(it)

                        onChangeValue(converted.join(third = false))
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.displayLarge.copy(
                        fontSize = fontSizeValue,
                        color = color,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    visualTransformation = visualTransformationAsCurrency(
                        currency = currency ?: ExtendCurrency(type = CurrencyType.NONE),
                        hintColor = color.copy(alpha = 0.2f),
                    ),

                    decorationBox = { input ->
                        Box(
                            Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(contentPaddingValues)
                        ) {
                            input()
                        }
                    }
                )
            }
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.displayLarge,
                fontSize = fontSizeValue,
                color = color,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                modifier = Modifier.padding(contentPaddingValues),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontSize = fontSizeLabel,
            color = color,
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
        EditorRow(
            value = "1 245 234 234 P",
            label = stringResource(id = R.string.budget_for_today),
        )
    }
}