package com.danilkinkin.buckwheat.utils

import android.content.res.Resources
import kotlin.math.roundToInt

fun Int.toSP(): Int = (this * Resources.getSystem().displayMetrics.scaledDensity).roundToInt()

fun Float.toSP(): Int = (this * Resources.getSystem().displayMetrics.scaledDensity).roundToInt()