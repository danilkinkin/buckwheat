package com.danilkinkin.buckwheat.util

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import kotlin.math.min

private fun getAnnotatedString(
    value: String,
    hintAfterIndex: Pair<Int, Int>,
    hintColor: Color,
): AnnotatedString {
    val builder = AnnotatedString.Builder(value)
    builder.addStyle(SpanStyle(color = hintColor), hintAfterIndex.first, hintAfterIndex.second)
    return builder.toAnnotatedString()
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

private fun calcMinShift(before: String, after: String): Int {
    var shift = 0

    if (before.isEmpty() || after.isEmpty()) return 0

    while (shift < after.length && (before.first() != after[shift])) {
        shift += 1
    }

    return shift
}

private fun visualTransformationAsCurrency(
    input: AnnotatedString,
    currency: ExtendCurrency,
    hintColor: Color,
): TransformedText {
    val floatDivider = getFloatDivider()
    val fixed = tryConvertStringToNumber(input.text)
    val output = prettyCandyCanes(
        input.text.ifEmpty { "0" }.toBigDecimal(),
        currency,
        maximumFractionDigits = 2,
        minimumFractionDigits = 1,
    )

    val offsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            val shift = calcShift(input.text.replace(".", floatDivider), output, offset)
            val minShift = calcMinShift(input.text.replace(".", floatDivider), output)

            return (offset + shift).coerceIn(minShift, output.length)
        }

        override fun transformedToOriginal(offset: Int): Int {
            val shift = calcShift(input.text.replace(".", floatDivider), output, offset)

            return (offset - shift).coerceIn(0, output.length)
        }
    }

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
    
    return TransformedText(
        getAnnotatedString(
            before + divider + after,
            Pair(
                before.length + (if (fixed.third.isNotEmpty()) 1 else 0),
                before.length + (if (fixed.third.isNotEmpty()) 2 else 0),
            ),
            hintColor,
        ),
        offsetTranslator,
    )
}

fun visualTransformationAsCurrency(
    currency: ExtendCurrency,
    hintColor: Color,
): ((input: AnnotatedString) -> TransformedText) {
    return {
        visualTransformationAsCurrency(it, currency, hintColor)
    }
}

fun isNumber(char: Char): Boolean {
    return try {
        char.toString().toInt(); true
    } catch (e: Exception) {
        false
    }
}

fun Triple<String, String, String>.join(third: Boolean = true): String = this.first + this.second + if (third) this.third else ""

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