package com.danilkinkin.buckwheat.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.res.ResourcesCompat
import androidx.glance.GlanceComposable
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import com.danilkinkin.buckwheat.R

fun drawText(context: Context, text: String): Bitmap {
    val myBitmap: Bitmap = Bitmap.createBitmap(160, 84, Bitmap.Config.ARGB_4444)
    val myCanvas = Canvas(myBitmap)
    val paint = Paint()
    val clock: Typeface = ResourcesCompat.getFont(context, R.font.manrope_bold)!!
    paint.isAntiAlias = true
    paint.isSubpixelText = true
    paint.typeface = clock
    paint.style = Paint.Style.FILL
    paint.color = if (context.isNightMode) Color.Black.toArgb() else Color.White.toArgb()
    paint.textSize = 65F
    paint.textAlign = Paint.Align.CENTER
    myCanvas.drawText(text, 80F, 60F, paint)
    return myBitmap
}

@Composable
@GlanceComposable
fun CanvasText(text: String) {
    val context = LocalContext.current

    Image(
        provider = ImageProvider(drawText(context, text)),
        contentDescription = null,
    )
}
