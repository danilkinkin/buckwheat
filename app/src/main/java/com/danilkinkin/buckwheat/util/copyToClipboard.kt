package com.danilkinkin.buckwheat.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.danilkinkin.buckwheat.R

fun copyLinkToClipboard(context: Context, link: String) {
    val intent = Intent(Intent.ACTION_VIEW)

    intent.data = Uri.parse(link)

    try {
        ContextCompat.startActivity(context, intent, null)
    } catch (e: Exception) {
        val clipboard = ContextCompat.getSystemService(
            context,
            ClipboardManager::class.java
        ) as ClipboardManager

        clipboard.setPrimaryClip(ClipData.newPlainText("url", link))

        Toast
            .makeText(
                context,
                context.getString(R.string.copy_in_clipboard),
                Toast.LENGTH_LONG
            )
            .show()
    }
}