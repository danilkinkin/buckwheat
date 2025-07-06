package com.danilkinkin.buckwheat.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.glance.ColorFilter
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.danilkinkin.buckwheat.base.Size


fun Paint.applyFontToPaint(context: Context, style: TextStyle): Paint {
    this.apply {
        isAntiAlias = true
        isSubpixelText = true

        val builder = Typeface.Builder(context.assets, "font/manrope_variable.ttf")

        val weight = when (style.fontWeight) {
            FontWeight.Normal -> 500
            FontWeight.Medium -> 700
            FontWeight.Bold -> 900
            else -> 800
        }

        builder.setFontVariationSettings("'wght' $weight")

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

    val bitmap: Bitmap = createBitmap(size.width, size.height)

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
                    style.copy(Color.Black.toColorProvider())
                )
            ),
            colorFilter = ColorFilter.tint(style.color),
            contentScale = ContentScale.Fit,
            contentDescription = null,
        )
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(300, 55)
@Composable
@GlanceComposable
fun PreviewColoredText() {
    GlanceTheme {
        CompositionLocalProvider(
            LocalContentColor provides GlanceTheme.colors.onSurface,
        ) {
            CanvasText(
                text = "Hey!",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = Color.Green.toColorProvider()
                )
            )
        }
    }
}


@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(300, 55)
@Composable
@GlanceComposable
fun PreviewWithEmojiTint() {
    GlanceTheme {
        CompositionLocalProvider(
            LocalContentColor provides GlanceTheme.colors.onSurface,
        ) {
            Row {
                CanvasText(
                    text = "Hey \uD83D\uDC4B\uD83C\uDFFB!",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(300, 55)
@Composable
@GlanceComposable
fun PreviewWithEmojiNoTint() {
    GlanceTheme {
        CompositionLocalProvider(
            LocalContentColor provides GlanceTheme.colors.onSurface,
        ) {
            Row {
                CanvasText(
                    text = "Hey ",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                    )
                )
                CanvasText(
                    text = "\uD83D\uDC4B\uD83C\uDFFB",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                    ),
                    noTint = true
                )
                CanvasText(
                    text = "!",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                    )
                )
            }
        }
    }
}
