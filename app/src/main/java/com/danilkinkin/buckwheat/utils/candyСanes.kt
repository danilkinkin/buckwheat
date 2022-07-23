package com.danilkinkin.buckwheat.utils

import com.danilkinkin.buckwheat.MainActivity
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

            if (currency !== null) return ExtendCurrency(value = value, type = CurrencyType.FROM_LIST)
            if (value === null || value.isNullOrEmpty()) return ExtendCurrency(value = null, type = CurrencyType.NONE)

            return ExtendCurrency(value = value, type = CurrencyType.CUSTOM)
        }
    }
}

fun Double.round(scale: Int): Double = BigDecimal(this).setScale(scale, RoundingMode.HALF_EVEN).toDouble()

fun prettyCandyCanes(
    double: Double,
    forceShowAfterDot: Boolean = false,
    currency: ExtendCurrency = MainActivity.getInstance().model.currency,
): String {
    val formatter = if (currency.type === CurrencyType.FROM_LIST) currencyFormat else numberFormat

    formatter.maximumFractionDigits = if (forceShowAfterDot) 5 else 2
    formatter.minimumFractionDigits = if (forceShowAfterDot) 1 else 0

    if (currency.type === CurrencyType.FROM_LIST) formatter.currency = Currency.getInstance(currency.value)

    var formattedValue = formatter.format(double)

    if (currency.type === CurrencyType.CUSTOM) formattedValue = "$formattedValue ${currency.value}"

    return formattedValue
}
