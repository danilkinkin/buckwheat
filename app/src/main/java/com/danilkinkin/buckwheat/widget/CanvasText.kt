package com.danilkinkin.buckwheat.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.glance.ColorFilter
import androidx.glance.GlanceComposable
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.GlanceModifier
import androidx.glance.layout.ContentScale
import androidx.glance.layout.collectPaddingInDp
import androidx.glance.layout.width
import androidx.glance.text.TextAlign
import androidx.glance.unit.ColorProvider
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.Size
import java.io.File


fun Paint.applyFontToPaint(context: Context, style: TextStyle): Paint {
    this.apply {
        isAntiAlias = true
        isSubpixelText = true

        val builder = Typeface.Builder(context.assets, "font/manrope_variable.ttf")


        Log.d("CanvasText", "builder: ${builder}")

        builder.setFontVariationSettings("'wght' 800")

        //builder.setWeight(800)

        typeface = builder.build()

        this.style = Paint.Style.FILL
        color = style.color.getColor(context).toArgb()
        textSize = Resources.getSystem().displayMetrics.density * style.fontSize!!.value
        textAlign = if (style.textAlign == TextAlign.Right) Paint.Align.RIGHT else Paint.Align.LEFT
    }

    return this
}

fun calcTextSize(context: Context, text: String, style: TextStyle): Size {
    val paint = Paint().applyFontToPaint(context, style)

    val textBounds = Rect()
    paint.getTextBounds(text, 0, text.length, textBounds)

    // Fix for calculate space charter width
    val spaceWidthTextBounds = Rect()
    paint.getTextBounds("n", 0, 1, spaceWidthTextBounds)

    val width = (textBounds.width() + paint.letterSpacing * 2 + textBounds.height()
        .toFloat() * 0.25F).toInt()

    return Size(
        if (width > 0) width else spaceWidthTextBounds.width(),
        (paint.descent() - paint.ascent()).toInt(),
    )
}

fun drawText(context: Context, text: String, style: TextStyle): Bitmap {
    val paint = Paint().applyFontToPaint(context, style)
    val size = calcTextSize(context, text, style)

    val bitmap: Bitmap = Bitmap.createBitmap(
        size.width,
        size.height,
        Bitmap.Config.ARGB_8888,
    )

    Canvas(bitmap).drawText(text, 0F, -paint.ascent(), paint)

    return bitmap
}

@Composable
@GlanceComposable
fun CanvasText(
    modifier: GlanceModifier = GlanceModifier,
    text: String,
    style: TextStyle,
    noTint: Boolean = false,
) {
    val context = LocalContext.current

    val size = calcTextSize(context, text, style)
    val width = 0.dp
        //.plus(modifier.collectPaddingInDp(context.resources)?.start ?: 0.dp)
        .plus(Dp(size.width / context.resources.displayMetrics.density))
        //.plus(modifier.collectPaddingInDp(context.resources)?.end ?: 0.dp)


    if (noTint) {
        Image(
            modifier = modifier.width(width),
            provider = ImageProvider(drawText(context, text, style)),
            contentScale = ContentScale.Fit,
            contentDescription = null,
        )
    } else {
        Image(
            modifier = modifier.width(width),
            provider = ImageProvider(
                drawText(
                    context,
                    text,
                    style.copy(ColorProvider(Color.Black))
                )
            ),
            colorFilter = ColorFilter.tint(style.color),
            contentScale = ContentScale.Fit,
            contentDescription = null,
        )
    }
}
