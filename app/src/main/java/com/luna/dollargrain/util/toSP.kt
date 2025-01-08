package com.luna.dollargrain.util

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Stable
fun min(a: TextUnit, b: TextUnit): TextUnit = kotlin.math.min(a.value, b.value).sp

@Stable
fun max(a: TextUnit, b: TextUnit): TextUnit = kotlin.math.max(a.value, b.value).sp