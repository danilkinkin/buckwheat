package com.luna.dollargrain.util

import android.content.Context
import com.luna.dollargrain.data.ExtendCurrency
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

// Get localised float divider (',' or '.')
fun getFloatDivider(): String {
    val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

    numberFormat.maximumFractionDigits = 1
    numberFormat.minimumFractionDigits = 1

    val formattedValue = numberFormat.format(1.0)

    return formattedValue.substring(1, 2)
}

fun numberFormat(
    context: Context,
    value: BigDecimal,
    currency: ExtendCurrency,
    trimDecimalPlaces: Boolean = false,
    forceShowAfterDot: Boolean = false,
    maximumFractionDigits: Int = if (forceShowAfterDot) 5 else 2,
    minimumFractionDigits: Int = if (forceShowAfterDot) 1 else 0,
): String {
    var valueFinal = value
    var decimalPlace = ""
    val formatter = if (currency.type === ExtendCurrency.Type.FROM_LIST) {
        NumberFormat.getCurrencyInstance(Locale.getDefault())
    } else {
        NumberFormat.getNumberInstance(Locale.getDefault())
    }

    formatter.maximumFractionDigits = maximumFractionDigits
    formatter.minimumFractionDigits = minimumFractionDigits

    if (currency.type === ExtendCurrency.Type.FROM_LIST) formatter.currency =
        Currency.getInstance(currency.value)

    val thousand = BigDecimal(1000)
    var overflow = false
    if (trimDecimalPlaces) {
        valueFinal = if (value >= thousand.pow(4) * BigDecimal(100)) {
            decimalPlace = "T"
            overflow = true
            100.toBigDecimal()
        } else if (value >= thousand.pow(4)) {
            decimalPlace = "T"
            value / (thousand.pow(4))
        } else if (value >= thousand.pow(3)) {
            decimalPlace = "B"
            value / (thousand.pow(3))
        } else if (value >= thousand.pow(2)) {
            decimalPlace = "M"
            value / (thousand.pow(2))
        } else if (value >= thousand * BigDecimal(100)) {
            decimalPlace = "K"
            value / thousand
        } else {
            value
        }
    }


    var formattedValue = formatter.format(valueFinal)

    if (currency.type === ExtendCurrency.Type.CUSTOM) formattedValue = "$formattedValue ${currency.value}"

    return "${if(overflow) "> " else ""}$formattedValue $decimalPlace"
}