package com.danilkinkin.buckwheat.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*


val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

enum class CurrencyType { FROM_LIST, CUSTOM, NONE }
class ExtendCurrency(val value: String? = null, val type: CurrencyType) {
    companion object {
        fun getInstance(value: String?): ExtendCurrency {


            val currency = try {
                Currency.getInstance(value)
            } catch (e: Exception) {
                null
            }

            if (currency !== null) return ExtendCurrency(
                value = value,
                type = CurrencyType.FROM_LIST
            )
            if (value === null || value.isNullOrEmpty() || value == "null") return ExtendCurrency(
                value = null,
                type = CurrencyType.NONE
            )

            return ExtendCurrency(value = value, type = CurrencyType.CUSTOM)
        }
    }
}

fun Double.round(scale: Int): Double =
    BigDecimal(this).setScale(scale, RoundingMode.HALF_EVEN).toDouble()

fun getFloatDivider(): String {
    val formatter = numberFormat

    formatter.maximumFractionDigits = 1
    formatter.minimumFractionDigits = 1

    val formattedValue = formatter.format(1.0)

    return formattedValue.substring(1, 2)
}

fun prettyCandyCanes(
    value: BigDecimal,
    currency: ExtendCurrency,
    forceShowAfterDot: Boolean = false,
    maximumFractionDigits: Int = if (forceShowAfterDot) 5 else 2,
    minimumFractionDigits: Int = if (forceShowAfterDot) 1 else 0,
): String {
    val formatter = if (currency.type === CurrencyType.FROM_LIST) currencyFormat else numberFormat

    formatter.maximumFractionDigits = maximumFractionDigits
    formatter.minimumFractionDigits = minimumFractionDigits

    if (currency.type === CurrencyType.FROM_LIST) formatter.currency =
        Currency.getInstance(currency.value)

    var formattedValue = formatter.format(value)

    if (currency.type === CurrencyType.CUSTOM) formattedValue = "$formattedValue ${currency.value}"

    return formattedValue
}