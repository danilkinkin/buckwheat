package com.danilkinkin.buckwheat.widget

import android.graphics.LightingColorFilter
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.*
import androidx.glance.layout.*
import androidx.glance.unit.ColorProvider
import com.danilkinkin.buckwheat.R

@Composable
@GlanceComposable
fun Wave(modifier: GlanceModifier = GlanceModifier, percent: Float, color: ColorProvider) {
    val context = LocalContext.current

    Row(modifier = modifier) {
        Box(
            modifier = GlanceModifier
                .fillMaxHeight()
                .width(60.dp)
                .background(color)
        ) {}

        val waveDrawable = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.wave,
            null,
        )!!

        val waveColor = color.getColor(context).toArgb()
        waveDrawable.colorFilter = LightingColorFilter(waveColor, waveColor)

        Box(
            modifier = GlanceModifier
                .fillMaxHeight()
                .background(color)
                .background(
                    imageProvider = ImageProvider(waveDrawable.toBitmap()),
                    contentScale = ContentScale.Crop
                )
        ) {}
    }
}