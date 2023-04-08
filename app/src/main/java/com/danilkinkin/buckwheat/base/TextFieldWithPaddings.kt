package com.danilkinkin.buckwheat.base

import android.util.Log
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import kotlinx.coroutines.CoroutineStart

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextFieldWithPaddings(
    value: String = "",
    onChangeValue: (output: String) -> Unit,
    contentPadding: PaddingValues = PaddingValues(horizontal = 32.dp),
    textStyle: TextStyle = MaterialTheme.typography.displayLarge,
    cursorBrush: Brush = SolidColor(MaterialTheme.colorScheme.primary),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }
    val textFieldValue = textFieldValueState.copy(text = value)

    val scrollState = rememberScrollState()
    var containerWidth by remember { mutableStateOf(0) }
    var inputWidth by remember { mutableStateOf(0) }
    var width by remember(value) { mutableStateOf(0) }
    var requestScrollToCursor by remember { mutableStateOf(false) }
    val layoutDirection = when (LocalConfiguration.current.layoutDirection) {
        0 -> LayoutDirection.Rtl
        1 -> LayoutDirection.Ltr
        else -> LayoutDirection.Rtl
    }

    var lastCursorPosition by remember { mutableStateOf(0) }
    var lastTextValue by remember(value) { mutableStateOf(value) }
    val coroutineScope = rememberCoroutineScope()
    val localDensity = LocalDensity.current
    val localContext = LocalContext.current
    val gapStart =
        with(localDensity) { contentPadding.calculateStartPadding(layoutDirection).toPx().toInt() }
    val gapEnd =
        with(localDensity) { contentPadding.calculateEndPadding(layoutDirection).toPx().toInt() }

    width = calculateIntrinsics(
        visualTransformation.filter(
            AnnotatedString(value)
        ).text.text,
        textStyle,
    ).maxIntrinsicWidth.toInt()

    fun restoreScrollPosition(forceEnd: Boolean = false) {
        val transformation = visualTransformation.filter(
            AnnotatedString(value)
        )

        val position = ParagraphIntrinsics(
            text = transformation.text.text.substring(
                0,
                transformation.offsetMapping.originalToTransformed(
                    if (forceEnd) value.length else textFieldValueState.selection.start,
                ).coerceAtMost(transformation.text.text.length),
            ),
            style = textStyle,
            density = localDensity,
            fontFamilyResolver = createFontFamilyResolver(localContext)
        ).maxIntrinsicWidth

        coroutineScope.launch {
            Log.d(
                "TextField",
                "position = ${position.toInt()} scrollState = ${scrollState.value} containerWidth = $containerWidth width = $width gapStart = $gapStart gapEnd = $gapEnd requestScrollToCursor = $requestScrollToCursor forceEnd = $forceEnd"
            )

            if (requestScrollToCursor || forceEnd) {
                if (position.toInt() - scrollState.value > containerWidth) {
                    Log.d(
                        "TextField",
                        "scroll ot cursor"
                    )
                    scrollState.animateScrollTo(
                        position.toInt() - containerWidth + gapEnd, tween(
                            durationMillis = 240,
                            easing = EaseInOutQuad,
                        )
                    )
                } else if (width < containerWidth) {
                    Log.d(
                        "TextField",
                        "scroll ot end"
                    )
                    scrollState.animateScrollTo(
                        containerWidth, tween(
                            durationMillis = 240,
                            easing = EaseInOutQuad,
                        )
                    )
                } else {
                    Log.d(
                        "TextField",
                        "skip"
                    )
                    requestScrollToCursor = false

                    if (scrollState.value < inputWidth - width + gapStart) {
                        scrollState.animateScrollTo(
                            inputWidth - width + gapStart, tween(
                                durationMillis = 240,
                                easing = EaseInOutQuad,
                            )
                        )
                    }
                }
            } else {
                Log.d("scroll", "fixed = ${scrollState.value < inputWidth - width + gapStart}")

                if (width > containerWidth) {
                    scrollState.animateScrollTo(
                        width, tween(
                            durationMillis = 240,
                            easing = EaseInOutQuad,
                        )
                    )
                }
            }
        }
    }

    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
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

                    val stringChangedSinceLastInvocation =
                        lastTextValue != newTextFieldValueState.text
                    val positionChangedSinceLastInvocation =
                        lastCursorPosition != newTextFieldValueState.selection.start

                    lastTextValue = newTextFieldValueState.text
                    lastCursorPosition = newTextFieldValueState.selection.start

                    if (stringChangedSinceLastInvocation || positionChangedSinceLastInvocation) {
                        requestScrollToCursor = true

                        onChangeValue(lastTextValue)
                    }
                },
                onTextLayout = { restoreScrollPosition() },
                textStyle = textStyle.copy(textAlign = TextAlign.End),
                singleLine = true,
                cursorBrush = cursorBrush,
                visualTransformation = visualTransformation,
                modifier = Modifier
                    .background(Color.Red.copy(alpha = 0.3f))
                    .disabledHorizontalPointerInputScroll(
                        scrollState.value,
                        inputWidth - width + gapStart,
                    )
                    .focusRequester(focusRequester),
                decorationBox = { input ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(contentPadding)
                            .background(Color.Yellow.copy(alpha = 0.3f))
                            .onGloballyPositioned {
                                inputWidth = it.size.width
                                Log.d("inputWidth", "inputWidth = $inputWidth")
                            }
                    ) {
                        input()
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        DisposableEffect(value) {
            if (textFieldValueState.selection.start != 0) {
                return@DisposableEffect onDispose { }
            }

            //restoreScrollPosition(true)

            onDispose { }
        }
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
            Log.d("onPreScroll", "valueScroll = $valueScroll available.x = ${available.x} breakpoint = $breakpoint")
            if ((valueScroll - available.x < breakpoint) && (available.x.toInt() > 0)) {
                Log.d("onPreScroll", "block = ${(valueScroll - available.x) - breakpoint}")
                return Offset(breakpoint - (valueScroll - available.x), 0f)
            }

            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity {
            return available.copy(y = 0f)
        }
    })
}