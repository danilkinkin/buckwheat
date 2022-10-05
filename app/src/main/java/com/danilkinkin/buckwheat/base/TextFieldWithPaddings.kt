package com.danilkinkin.buckwheat.base

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphIntrinsics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.launch
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*

@Composable
fun TextFieldWithPaddings(
    value: String = "",
    onChangeValue: (output: String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(horizontal = 32.dp),
    textStyle: TextStyle = MaterialTheme.typography.displayLarge,
    cursorBrush: Brush = SolidColor(MaterialTheme.colorScheme.primary),
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }
    val textFieldValue = textFieldValueState.copy(text = value)

    val scrollState = rememberScrollState()
    var containerWidth by remember { mutableStateOf(0) }
    var width by remember(value) { mutableStateOf(0) }
    var requestScrollToCursor by remember { mutableStateOf(false) }
    val layoutDirection =  when (LocalConfiguration.current.layoutDirection) {
        0 -> LayoutDirection.Rtl
        1 -> LayoutDirection.Ltr
        else -> LayoutDirection.Rtl
    }

    var lastCursorPosition by remember { mutableStateOf(0) }
    var lastTextValue by remember(value) { mutableStateOf(value) }
    val coroutineScope = rememberCoroutineScope()
    val localDensity = LocalDensity.current
    val localContext = LocalContext.current
    val gapStart = with(localDensity) { contentPadding.calculateStartPadding(layoutDirection).toPx().toInt() }
    val gapEnd = with(localDensity) { contentPadding.calculateEndPadding(layoutDirection).toPx().toInt() }

    width = calculateIntrinsics(
        visualTransformation.filter(
            AnnotatedString(value)
        ).text.text,
        textStyle,
    ).maxIntrinsicWidth.toInt()

    fun restoreScrollPosition(forceEnd: Boolean = false) {
        if (!requestScrollToCursor && !forceEnd) return

        val transformation = visualTransformation.filter(
            AnnotatedString(value)
        )

        val position = ParagraphIntrinsics(
            text = transformation.text.text.substring(
                0,
                transformation.offsetMapping.originalToTransformed(
                    if (forceEnd) value.length else textFieldValueState.selection.start,
                ),
            ),
            style = textStyle,
            density = localDensity,
            fontFamilyResolver = createFontFamilyResolver(localContext)
        ).maxIntrinsicWidth

        coroutineScope.launch {
            if (position.toInt() - scrollState.value < 0) {
                scrollState.scrollTo(position.toInt())
            } else if (position.toInt() - scrollState.value + gapStart + gapEnd > containerWidth) {
                scrollState.scrollTo(position.toInt() - containerWidth + gapStart + gapEnd)
            } else if (scrollState.value + containerWidth > width + gapStart + gapEnd) {
                scrollState.scrollTo(position.toInt() - containerWidth + gapStart + gapEnd)
            } else {
                requestScrollToCursor = false
            }
        }
    }

    DisposableEffect(value) {
        if (textFieldValueState.selection.start != 0) {
            return@DisposableEffect onDispose { }
        }

        restoreScrollPosition(true)

        onDispose {  }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                containerWidth = it.size.width
            }
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newTextFieldValueState ->
                textFieldValueState = newTextFieldValueState

                val stringChangedSinceLastInvocation = lastTextValue != newTextFieldValueState.text
                val positionChangedSinceLastInvocation = lastCursorPosition != newTextFieldValueState.selection.start

                lastTextValue = newTextFieldValueState.text
                lastCursorPosition = newTextFieldValueState.selection.start

                if (stringChangedSinceLastInvocation || positionChangedSinceLastInvocation) {
                    requestScrollToCursor = true

                    onChangeValue(lastTextValue)
                }

                Log.d("textFieldValueState", "request scroll... $requestScrollToCursor")
            },
            onTextLayout = { restoreScrollPosition() },
            textStyle = textStyle,
            singleLine = true,
            cursorBrush = cursorBrush,
            visualTransformation = visualTransformation,
            modifier = Modifier.disabledHorizontalPointerInputScroll(
                scrollState.value + containerWidth,
                width + gapStart + gapEnd,
            ),
            decorationBox = { input ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(contentPadding)
                ) {
                    input()
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
    }
}

@Composable
fun calculateIntrinsics(input: String, style: TextStyle): ParagraphIntrinsics {
    return ParagraphIntrinsics(
        text = input,
        style = style,
        density = LocalDensity.current,
        fontFamilyResolver = createFontFamilyResolver(LocalContext.current)
    )
}

fun Modifier.disabledHorizontalPointerInputScroll(
    valueScroll: Int,
    breakpoint: Int,
): Modifier {
    return this.nestedScroll(object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if ((valueScroll - available.x > breakpoint) && (valueScroll + available.x.toInt() > 0)) {
                return Offset(breakpoint - (valueScroll - available.x), 0f)
            }

            return Offset.Zero
        }
        override suspend fun onPreFling(available: Velocity): Velocity {
            return available
        }
    })
}