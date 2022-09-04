package com.danilkinkin.buckwheat.util

import androidx.compose.ui.graphics.Color

fun combineColors(colorA: Color, colorB: Color, angle: Float = 0.5F): Color {
    val colorAPart = (1F - angle) * 2
    val colorBPart = angle * 2

    return Color(
        red = (colorA.red * colorAPart + colorB.red * colorBPart) / 2,
        green = (colorA.green * colorAPart + colorB.green * colorBPart) / 2,
        blue = (colorA.blue * colorAPart + colorB.blue * colorBPart) / 2,
    )
}