package com.luna.dollargrain.widget.minimal

import android.content.Context
import com.luna.dollargrain.widget.WidgetReceiver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MinimalWidgetReceiver : WidgetReceiver() {
    companion object {
        fun requestUpdateData(context: Context) {
            requestUpdateData(context, MinimalWidgetReceiver::class.java)
        }
    }

    override val glanceAppWidget = MinimalWidget()
}
