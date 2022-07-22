package com.danilkinkin.buckwheat.utils

import android.util.Log
import com.danilkinkin.buckwheat.MainActivity
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
val numberFormat: NumberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

fun Double.round(scale: Int): Double = BigDecimal(this).setScale(scale, RoundingMode.HALF_EVEN).toDouble()

fun prettyCandyCanes(double: Double, forceShowAfterDot: Boolean = false, currency: Currency? = MainActivity.getInstance().model.currency): String {
    Log.d("prettyCandyCanes", "currency = $currency")

    val formatter = if (currency !== null) currencyFormat else numberFormat

    formatter.maximumFractionDigits = if (forceShowAfterDot) 5 else 2
    formatter.minimumFractionDigits = if (forceShowAfterDot) 1 else 0

    if (currency !== null) formatter.currency = currency

    return formatter.format(double)
}
