package com.danilkinkin.buckwheat.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.danilkinkin.buckwheat.R

@Composable
fun Label(modifier: GlanceModifier = GlanceModifier, text: String) {
    val color = R.color.material_dynamic_secondary70

    Text(
        modifier = modifier,
        text = text,
        style = TextStyle(
            color = ColorProvider(color),
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
        )
    )
}