package com.danilkinkin.buckwheat.utils

import android.view.View

fun getStatusBarHeight(view: View): Int {
    val resourceId = view.resources.getIdentifier("status_bar_height", "dimen", "android")

    return if (resourceId > 0) {
        view.resources.getDimensionPixelSize(resourceId)
    } else {
        0
    }
}

