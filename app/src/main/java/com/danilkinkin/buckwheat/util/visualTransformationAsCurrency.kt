package com.danilkinkin.buckwheat.util

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.tooling.preview.Preview
import com.danilkinkin.buckwheat.data.ExtendCurrency
import java.math.BigDecimal
import kotlin.math.min

private fun getAnnotatedString(
    value: String,
    hintParts: List<Pair<Int, Int>>,
    styles: List<SpanStyle>,
): AnnotatedString {
    val builder = AnnotatedString.Builder(value)
    hintParts.forEachIndexed { index, part ->
        builder.addStyle(styles[index], part.first, part.second)
    }
    return builder.toAnnotatedString()
}

private fun getAnnotatedString(
    value: String,
    hintParts: List<Pair<Int, Int>>,
    hintColor: Color,
): AnnotatedString {
    return getAnnotatedString(value, hintParts, hintParts.map { SpanStyle(color = hintColor) })
}

private fun getAnnotatedString(
    value: String,
    hintPart: Pair<Int, Int>,
    hintColor: Color,
): AnnotatedString {
    return getAnnotatedString(value, listOf(hintPart), hintColor)
}

private fun calcShift(before: String, after: String, position: Int): Int {
    var shift = 0

    for (i in 0 until position) {
        while (i < before.length && i + shift < after.length && (before[i] != after[i + shift])) {
            shift += 1
        }
    }

    return shift
}

private fun visualTransformationAsCurrency(
    context: Context,
    input: AnnotatedString,
    currency: ExtendCurrency,
    hintColor: Color,
): TransformedText {
    val floatDivider = getFloatDivider()
    val fixed = tryConvertStringToNumber(input.text)
    val currSymbol = numberFormat(
        context,
        BigDecimal.ZERO,
        currency,
        maximumFractionDigits = 0,
        minimumFractionDigits = 0,
    ).filter { it != '0' }
    var output = numberFormat(
        context,
        input.text.ifEmpty { "0" }.toBigDecimal(),
        currency,
        maximumFractionDigits = 2,
        minimumFractionDigits = 1,
    ).replace(currSymbol, "").trim()

    val forceShowAfterDot = input.text.contains(".0")
    val before = output.substringBefore("${floatDivider}0")
    val after = if (forceShowAfterDot) {
        output.substringAfter(floatDivider, "")
    } else {
        output.substringAfter("${floatDivider}0", "")
    }

    val divider = if (fixed.third.isNotEmpty() || forceShowAfterDot) {
        "$floatDivider${fixed.third}"
    } else {
        ""
    }

    output = if (input.text.isEmpty()) "" else before + divider + after

    val offsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            val shift = calcShift(input.text.replace(".", floatDivider), output, offset)

            return (offset + shift).coerceIn(0, output.length)
        }

        override fun transformedToOriginal(offset: Int): Int {
            val shift = calcShift(input.text.replace(".", floatDivider), output, offset)

            return (offset - shift).coerceIn(0, input.length)
        }
    }

    return if (input.text.isEmpty()) {
        TransformedText(
            getAnnotatedString(
                output,
                listOf(),
                listOf(),
            ),
            offsetTranslator,
        )
    } else {
        TransformedText(
            getAnnotatedString(
                output,
                listOf(
                    Pair(
                        before.length + (if (fixed.third.isNotEmpty()) 1 else 0),
                        before.length + (if (fixed.third.isNotEmpty()) 2 else 0),
                    ),
                ),
                listOf(
                    SpanStyle(color = hintColor),
                ),
            ),
            offsetTranslator,
        )
    }
}

fun visualTransformationAsCurrency(
    context: Context,
    currency: ExtendCurrency,
    hintColor: Color,
): ((input: AnnotatedString) -> TransformedText) {
    return {
        visualTransformationAsCurrency(context, it, currency, hintColor)
    }
}

fun isNumber(char: Char): Boolean {
    return try {
        char.toString().toInt(); true
    } catch (e: Exception) {
        false
    }
}

fun Triple<String, String, String>.join(third: Boolean = true): String =
    this.first + this.second + if (third) this.third else ""

fun fixedNumberString(input: String): String {
    val dotExist = input.contains(".")
    val before = input.substringBefore(".")
    val after = input.substringAfter(".", "")

    var addZero = false
    var beforeFiltered = before
        .replace("\\D".toRegex(), "")
        .trimStart { addZero = addZero || it == '0'; it == '0'}

    if (addZero) beforeFiltered = "0$beforeFiltered"
    addZero = false

    var afterFiltered = after
        .replace("\\D".toRegex(), "")
        .trimEnd { addZero = addZero || it == '0'; it == '0' }

    if (addZero) afterFiltered = "${afterFiltered}0"

    if (afterFiltered.length > 2) afterFiltered = afterFiltered.dropLast(afterFiltered.length - 2)

    if (beforeFiltered.isEmpty() && afterFiltered.isEmpty()) return ""

    if (afterFiltered.isEmpty() && !dotExist) return beforeFiltered

    return "$beforeFiltered.$afterFiltered"
}

fun tryConvertStringToNumber(input: String): Triple<String, String, String> {
    val afterDot = input.dropWhile { it != '.' }
    val beforeDot = input.substring(0, input.length - afterDot.length)

    val start = beforeDot.filter { isNumber(it) }.dropWhile { it == '0' }
    val hintStart = if (start.isEmpty()) "0" else ""
    val end = afterDot.filter { isNumber(it) }
    var hintEnd = ""
    if (end.isEmpty() && input.lastOrNull() == '.') {
        hintEnd = "0"
    }
    val middle = if (end.isNotEmpty() || (input.lastOrNull() == '.')) {
        "."
    } else {
        ""
    }

    return Triple(
        hintStart,
        "$start$middle${end.substring(0, min(2, end.length))}",
        hintEnd,
    )
}

@Preview
@Composable
fun Preview() {
    val context = LocalContext.current

    Column {
        Text(
            text = visualTransformationAsCurrency(
                context,
                getAnnotatedString("0", Pair(0, 1), Color.Green),
                currency = ExtendCurrency.none(),
                Color.Green,
            ).text
        )
        Text(
            text = visualTransformationAsCurrency(
                context,
                getAnnotatedString("0", Pair(0, 4), Color.Green),
                currency = ExtendCurrency.getInstance("EUR"),
                Color.Green,
            ).text
        )
        Text(
            text = visualTransformationAsCurrency(
                context,
                getAnnotatedString("0", Pair(0, 4), Color.Green),
                currency = ExtendCurrency.getInstance("RUB"),
                Color.Green,
            ).text
        )
        Text(
            text = visualTransformationAsCurrency(
                context,
                getAnnotatedString("", Pair(0, 4), Color.Green),
                currency = ExtendCurrency.none(),
                Color.Green,
            ).text
        )
        Text(
            text = visualTransformationAsCurrency(
                context,
                getAnnotatedString("", Pair(0, 4), Color.Green),
                currency = ExtendCurrency.getInstance("EUR"),
                Color.Green,
            ).text
        )
        Text(
            text = visualTransformationAsCurrency(
                context,
                getAnnotatedString("", Pair(0, 4), Color.Green),
                currency = ExtendCurrency.getInstance("RUB"),
                Color.Green,
            ).text
        )
    }
}