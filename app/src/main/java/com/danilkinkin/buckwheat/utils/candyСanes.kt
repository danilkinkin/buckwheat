package com.danilkinkin.buckwheat.utils

fun prettyCandyCanes(double: Double, forceShowAfterDot: Boolean = false): String {
    var str = double.toString()

    str = if (str.substringAfter(".") == "0" && !forceShowAfterDot) {
        str.substringBefore(".")
    } else {
        str
    }

    return str
}
