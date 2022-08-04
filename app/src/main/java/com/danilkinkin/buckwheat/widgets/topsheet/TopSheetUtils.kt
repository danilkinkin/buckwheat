package com.danilkinkin.buckwheat.widgets.topsheet

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import androidx.annotation.StyleableRes
import androidx.appcompat.content.res.AppCompatResources

object TopSheetUtils {
    /**
     * Returns the [ColorStateList] from the given [TypedArray] attributes. The resource
     * can include themeable attributes, regardless of API level.
     */
    fun getColorStateList(
        context: Context, attributes: TypedArray, @StyleableRes index: Int
    ): ColorStateList? {
        if (attributes.hasValue(index)) {
            val resourceId = attributes.getResourceId(index, 0)
            if (resourceId != 0) {
                val value = AppCompatResources.getColorStateList(context, resourceId)
                if (value != null) {
                    return value
                }
            }
        }

        return attributes.getColorStateList(index)
    }
}