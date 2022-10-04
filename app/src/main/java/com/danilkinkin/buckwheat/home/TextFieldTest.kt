package com.danilkinkin.buckwheat.home

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danilkinkin.buckwheat.base.TextFieldWithPaddings
import com.danilkinkin.buckwheat.util.CurrencyType
import com.danilkinkin.buckwheat.util.ExtendCurrency
import com.danilkinkin.buckwheat.util.visualTransformationAsCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldTest() {
    var value by remember { mutableStateOf("") }

    val statusBarHeight = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()

    Column {
        Spacer(modifier = Modifier.height(statusBarHeight))

        TextFieldWithPaddings(
            value = value,
            onChangeValue = {
                value = it
            },
            contentPadding = PaddingValues(horizontal = 32.dp),
            textStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 160.sp),
            visualTransformation = visualTransformationAsCurrency(
                currency = ExtendCurrency(type = CurrencyType.NONE),
                hintColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            ),
        )

        OutlinedTextField(
            value = value,
            onValueChange = {
                value = it
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.displayLarge,
        )

        BasicTextFieldBBB(
            value = value,
            onChangeValue = {
                value = it
            },
        )
    }
}

@Composable
fun BasicTextFieldBBB(
    value: String,
    onChangeValue: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = TextStyle.Default,
    singleLine: Boolean = false,
    maxLines: Int = Int.MAX_VALUE,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    cursorBrush: Brush = SolidColor(androidx.compose.ui.graphics.Color.Black),
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit =
        @Composable { innerTextField -> innerTextField() }
) {
    // Holds the latest internal TextFieldValue state. We need to keep it to have the correct value
    // of the composition.
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }
    // Holds the latest TextFieldValue that BasicTextField was recomposed with. We couldn't simply
    // pass `TextFieldValue(text = value)` to the CoreTextField because we need to preserve the
    // composition.
    val textFieldValue = textFieldValueState.copy(text = value)
    // Last String value that either text field was recomposed with or updated in the onValueChange
    // callback. We keep track of it to prevent calling onValueChange(String) for same String when
    // CoreTextField's onValueChange is called multiple times without recomposition in between.
    var lastTextValue by remember(value) { mutableStateOf(value) }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newTextFieldValueState ->
            textFieldValueState = newTextFieldValueState

            val stringChangedSinceLastInvocation = lastTextValue != newTextFieldValueState.text
            lastTextValue = newTextFieldValueState.text

            if (stringChangedSinceLastInvocation) {
                onChangeValue(newTextFieldValueState.text)
            }
        },
        modifier = modifier,
        textStyle = textStyle,
        visualTransformation = visualTransformation,
        onTextLayout = onTextLayout,
        interactionSource = interactionSource,
        cursorBrush = cursorBrush,
        singleLine = singleLine,
        decorationBox = decorationBox,
        enabled = enabled,
        readOnly = readOnly
    )
}

