package com.danilkinkin.buckwheat.util

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Stable
fun min(a: TextUnit, b: TextUnit): TextUnit = kotlin.math.min(a.value, b.value).sp