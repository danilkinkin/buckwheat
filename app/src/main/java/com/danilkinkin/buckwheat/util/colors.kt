package com.danilkinkin.buckwheat.util

import androidx.compose.ui.graphics.Color
import kotlin.math.ceil
import kotlin.math.floor

fun combineColors(colorA: Color, colorB: Color, angle: Float = 0.5F): Color {
    val colorAPart = (1F - angle) * 2
    val colorBPart = angle * 2

    return Color(
        red = (colorA.red * colorAPart + colorB.red * colorBPart) / 2,
        green = (colorA.green * colorAPart + colorB.green * colorBPart) / 2,
        blue = (colorA.blue * colorAPart + colorB.blue * colorBPart) / 2,
    )
}

fun combineColors(colors: List<Color>, angle: Float = 0.5F): Color {
    val approximateIndex = (colors.size - 1) * angle
    val colorA = colors[floor(approximateIndex).toInt()]
    val colorB = colors[ceil(approximateIndex).toInt()]

    return combineColors(colorA, colorB, approximateIndex - floor(approximateIndex))
}