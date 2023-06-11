package com.danilkinkin.buckwheat.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.runtime.Composable
import androidx.core.content.res.ResourcesCompat
import androidx.glance.ColorFilter
import androidx.glance.GlanceComposable
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.text.TextAlign
import com.danilkinkin.buckwheat.R

@Composable
fun drawText(context: Context, text: String, style: TextStyle): Bitmap {
    val paint = Paint().apply {
        isAntiAlias = true
        isSubpixelText = true
        typeface = ResourcesCompat.getFont(
            context,
            when (style.fontWeight) {
                FontWeight.Bold -> R.font.manrope_bold
                FontWeight.Medium -> R.font.manrope_medium
                FontWeight.Normal -> R.font.manrope_regular
                else -> R.font.manrope_regular
            },
        )!!
        this.style = Paint.Style.FILL
        textSize = Resources.getSystem().displayMetrics.density * style.fontSize!!.value
        textAlign = if (style.textAlign == TextAlign.Right) Paint.Align.RIGHT else Paint.Align.LEFT
    }

    val textBounds = Rect()
    paint.getTextBounds(text, 0, text.length, textBounds)

    val bitmap: Bitmap = Bitmap.createBitmap(
        (textBounds.width() + paint.letterSpacing * 2 + textBounds.height().toFloat() * 0.25F).toInt(),
        (paint.descent() - paint.ascent()).toInt(),
        Bitmap.Config.ARGB_8888,
    )

    Canvas(bitmap).drawText(text, 0F, -paint.ascent(), paint)

    return bitmap
}

@Composable
@GlanceComposable
fun CanvasText(modifier: GlanceModifier = GlanceModifier, text: String, style: TextStyle) {
    val context = LocalContext.current

    Image(
        modifier = modifier,
        provider = ImageProvider(drawText(context, text, style)),
        colorFilter = ColorFilter.tint(style.color),
        contentScale = ContentScale.Fit,
        contentDescription = null,
    )
}
