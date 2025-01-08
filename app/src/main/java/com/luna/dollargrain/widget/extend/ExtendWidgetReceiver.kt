package com.luna.dollargrain.widget.extend

import android.content.Context
import com.luna.dollargrain.widget.WidgetReceiver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExtendWidgetReceiver : WidgetReceiver() {
    companion object {
        fun requestUpdateData(context: Context) {
            requestUpdateData(context, ExtendWidgetReceiver::class.java)
        }
    }

    override val glanceAppWidget = ExtendWidget()
}
