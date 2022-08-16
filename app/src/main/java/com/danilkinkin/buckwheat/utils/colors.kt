package com.danilkinkin.buckwheat.utils

import android.content.Context
import android.content.res.TypedArray
import android.util.TypedValue


fun getThemeColor(context: Context, color: Int): Int {
    val typedValue = TypedValue()

    val a: TypedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(color))
    val colorId = a.getColor(0, 0)

    a.recycle()

    return colorId
}
