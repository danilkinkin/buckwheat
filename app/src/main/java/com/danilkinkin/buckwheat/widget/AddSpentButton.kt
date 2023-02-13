package com.danilkinkin.buckwheat.widget

import android.graphics.LightingColorFilter
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.danilkinkin.buckwheat.R

@Composable
fun AddSpentButton() {
    val context = LocalContext.current
    val color = R.color.material_dynamic_primary40

    Row(
        modifier = GlanceModifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = context.resources.getString(R.string.add_spent), style = TextStyle(
                color = ColorProvider(color),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        )
        Spacer(modifier = GlanceModifier.width(8.dp))

        val drawable = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.ic_add,
            null,
        )!!

        val iconColor = ContextCompat.getColor(context, color)
        drawable.colorFilter = LightingColorFilter(iconColor, iconColor)

        Image(
            modifier = GlanceModifier.size(24.dp),
            provider = ImageProvider(drawable.toBitmap()),
            contentDescription = null,
        )
    }
}