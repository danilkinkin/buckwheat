package com.luna.dollargrain.util

import android.content.Context
import com.luna.dollargrain.appLocale
import com.luna.dollargrain.systemLocale

fun collectEnvInfo(context: Context): String {
    return """
        ---- Device info ---------------------------
        Device: ${android.os.Build.DEVICE}
        Model: ${android.os.Build.MODEL}
        Product: ${android.os.Build.PRODUCT}
        Android version: ${android.os.Build.VERSION.RELEASE}
        SDK: ${android.os.Build.VERSION.SDK_INT}
        ---- Locale info ---------------------------
        App locale: ${context.appLocale}
        System locale: ${context.systemLocale}
    """.trimIndent()
}