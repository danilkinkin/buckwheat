package com.danilkinkin.buckwheat.utils

import android.content.res.Resources
import kotlin.math.roundToInt

fun Int.toDP(): Int = (this * Resources.getSystem().displayMetrics.density).roundToInt()

fun Float.toDP(): Int = (this * Resources.getSystem().displayMetrics.density).roundToInt()

fun Double.toDP(): Int = (this * Resources.getSystem().displayMetrics.density).roundToInt()