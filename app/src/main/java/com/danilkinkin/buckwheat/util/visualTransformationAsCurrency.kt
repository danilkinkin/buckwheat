package com.danilkinkin.buckwheat.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import kotlin.math.min

private fun visualTransformationAsCurrency(
    input: AnnotatedString,
    currency: ExtendCurrency,
    forceShowAfterDot: Boolean = false,
): TransformedText {
    val output = prettyCandyCanes(input.text.toBigDecimal(), forceShowAfterDot, currency)

    val offsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            val count = output.substring(0, min(offset, output.length - 1)).filter { !it.isDigit() }.length

            return min(offset + count, output.length)
        }

        override fun transformedToOriginal(offset: Int): Int {
            val count = output.substring(0, min(offset, output.length - 1)).filter { !it.isDigit() }.length

            return min(offset - count, output.length)
        }
    }

    return TransformedText(AnnotatedString(output), offsetTranslator)
}

fun visualTransformationAsCurrency(
    forceShowAfterDot: Boolean = false,
    currency: ExtendCurrency
): ((input: AnnotatedString) -> TransformedText) {
    return {
        visualTransformationAsCurrency(it, currency, forceShowAfterDot)
    }
}