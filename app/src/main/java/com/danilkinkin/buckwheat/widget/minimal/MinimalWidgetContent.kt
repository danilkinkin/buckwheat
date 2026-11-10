package com.danilkinkin.buckwheat.widget.minimal

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceComposable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import com.danilkinkin.buckwheat.MainActivity
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.widget.CanvasText
import com.danilkinkin.buckwheat.widget.LocalAccentColor
import com.danilkinkin.buckwheat.widget.LocalContentColor
import com.danilkinkin.buckwheat.widget.WidgetReceiver
import java.math.BigDecimal

/**
 * The small strip widget: today's spending and a tap target. The budget state that used to drive
 * this widget is gone, so the strip is always meaningful instead of saying "budget not set".
 */
@Composable
@GlanceComposable
fun MinimalWidgetContent() {
    val size = LocalSize.current
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val addIntent = Intent(context, MainActivity::class.java).apply {
        putExtra(MainActivity.EXTRA_OPEN_ADD, true)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val prefs = currentState<Preferences>()
    val todaySpent = BigDecimal(prefs[WidgetReceiver.todaySpentPreferenceKey] ?: "0")
    val currency = ExtendCurrency.getInstance(prefs[WidgetReceiver.currencyPreferenceKey])
    val isSmall = size == MinimalWidget.smallMode

    CompositionLocalProvider(
        LocalContentColor provides GlanceTheme.colors.onSurface,
        LocalAccentColor provides GlanceTheme.colors.primary,
    ) {
        val contentColor = LocalContentColor.current
        val accentColor = LocalAccentColor.current

        Box(
            modifier = GlanceModifier
                .cornerRadius(48.dp)
                .fillMaxSize()
                .background(ImageProvider(R.drawable.minimal_widget_preview_background))
                .clickable(actionStartActivity(addIntent)),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start,
                ) {
                    if (!isSmall) {
                        CanvasText(
                            text = context.resources.getString(R.string.today),
                            style = TextStyle(
                                color = contentColor,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                            ),
                            noTint = true,
                        )
                    }

                    CanvasText(
                        text = numberFormat(context, todaySpent, currency),
                        style = TextStyle(
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = when (size) {
                                MinimalWidget.largeMode -> 24.sp
                                MinimalWidget.smallMode -> 18.sp
                                else -> 20.sp
                            },
                        ),
                        noTint = true,
                    )
                }

                Spacer(GlanceModifier.defaultWeight())

                val drawable = ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.ic_add,
                    null,
                )!!

                Image(
                    modifier = GlanceModifier.size(
                        when (size) {
                            MinimalWidget.largeMode -> 32.dp
                            MinimalWidget.smallMode -> 24.dp
                            else -> 28.dp
                        }
                    ),
                    provider = ImageProvider(drawable.toBitmap()),
                    colorFilter = ColorFilter.tint(accentColor),
                    contentDescription = null,
                )
            }
        }
    }
}
