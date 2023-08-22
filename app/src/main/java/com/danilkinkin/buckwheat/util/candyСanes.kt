package com.danilkinkin.buckwheat.util

import com.danilkinkin.buckwheat.data.ExtendCurrency
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*



fun BigDecimal.isZero(): Boolean = this.signum() == 0

fun Float.clamp(min: Float, max: Float): Float = (1f - ((this.coerceIn(min, max) - min) / (max - min)))

fun Double.round(scale: Int): Double =
    BigDecimal(this).setScale(scale, RoundingMode.HALF_EVEN).toDouble()

fun getFloatDivider(): String {
    val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

    numberFormat.maximumFractionDigits = 1
    numberFormat.minimumFractionDigits = 1

    val formattedValue = numberFormat.format(1.0)

    return formattedValue.substring(1, 2)
}

fun prettyCandyCanes(
    value: BigDecimal,
    currency: ExtendCurrency,
    forceShowAfterDot: Boolean = false,
    maximumFractionDigits: Int = if (forceShowAfterDot) 5 else 2,
    minimumFractionDigits: Int = if (forceShowAfterDot) 1 else 0,
): String {
    val formatter = if (currency.type === ExtendCurrency.Type.FROM_LIST) {
        NumberFormat.getCurrencyInstance(Locale.getDefault())
    } else {
        NumberFormat.getNumberInstance(Locale.getDefault())
    }

    formatter.maximumFractionDigits = maximumFractionDigits
    formatter.minimumFractionDigits = minimumFractionDigits

    if (currency.type === ExtendCurrency.Type.FROM_LIST) formatter.currency =
        Currency.getInstance(currency.value)

    var formattedValue = formatter.format(value)

    if (currency.type === ExtendCurrency.Type.CUSTOM) formattedValue = "$formattedValue ${currency.value}"

    return formattedValue
}