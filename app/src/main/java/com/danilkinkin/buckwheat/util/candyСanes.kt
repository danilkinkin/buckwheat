package com.danilkinkin.buckwheat.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*



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
            if (value.isNullOrEmpty() || value == "null") return ExtendCurrency(
                value = null,
                type = CurrencyType.NONE
            )

            return ExtendCurrency(value = value, type = CurrencyType.CUSTOM)
        }

        fun none(): ExtendCurrency {
            return ExtendCurrency(type = CurrencyType.NONE)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is ExtendCurrency) return false

        return other.value == this.value && this.type == this.type
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + type.hashCode()
        return result
    }
}

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
    val formatter = if (currency.type === CurrencyType.FROM_LIST) {
        NumberFormat.getCurrencyInstance(Locale.getDefault())
    } else {
        NumberFormat.getNumberInstance(Locale.getDefault())
    }

    formatter.maximumFractionDigits = maximumFractionDigits
    formatter.minimumFractionDigits = minimumFractionDigits

    if (currency.type === CurrencyType.FROM_LIST) formatter.currency =
        Currency.getInstance(currency.value)

    var formattedValue = formatter.format(value)

    if (currency.type === CurrencyType.CUSTOM) formattedValue = "$formattedValue ${currency.value}"

    return formattedValue
}