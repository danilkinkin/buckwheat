package com.danilkinkin.buckwheat.widget.extend

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
import androidx.glance.layout.height
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
 * The big widget: today's spending as the headline, this month underneath when there is room.
 * It used to show the remaining daily budget, which no longer exists in a ledger-only app.
 */
@Composable
@GlanceComposable
fun ExtendWidgetContent() {
    val size = LocalSize.current
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java)

    val prefs = currentState<Preferences>()
    val todaySpent = BigDecimal(prefs[WidgetReceiver.todaySpentPreferenceKey] ?: "0")
    val monthSpent = BigDecimal(prefs[WidgetReceiver.monthSpentPreferenceKey] ?: "0")
    val currency = ExtendCurrency.getInstance(prefs[WidgetReceiver.currencyPreferenceKey])

    val isTiny = size == ExtendWidget.tinyMode || size == ExtendWidget.smallMode
    val showMonth = !isTiny

    CompositionLocalProvider(
        LocalContentColor provides GlanceTheme.colors.onSurface,
        LocalAccentColor provides GlanceTheme.colors.primary,
    ) {
        val contentColor = LocalContentColor.current
        val accentColor = LocalAccentColor.current

        Box(
            modifier = GlanceModifier
                .cornerRadius(32.dp)
                .fillMaxSize()
                .background(ImageProvider(R.drawable.extend_widget_background))
                .clickable(actionStartActivity(intent))
                .padding(
                    when (size) {
                        ExtendWidget.superHugeMode, ExtendWidget.hugeMode -> 8.dp
                        else -> 0.dp
                    }
                )
        ) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(
                        start = 24.dp,
                        top = when (size) {
                            ExtendWidget.tinyMode -> 16.dp
                            ExtendWidget.smallMode -> 18.dp
                            else -> 24.dp
                        },
                        end = 24.dp,
                        bottom = 24.dp,
                    ),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                CanvasText(
                    text = context.resources.getString(R.string.today),
                    style = TextStyle(
                        color = contentColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = when (size) {
                            ExtendWidget.superHugeMode, ExtendWidget.hugeMode -> 16.sp
                            else -> 12.sp
                        },
                    ),
                    noTint = true,
                )

                Spacer(GlanceModifier.height(4.dp))

                CanvasText(
                    text = numberFormat(context, todaySpent, currency),
                    style = TextStyle(
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = when (size) {
                            ExtendWidget.superHugeMode -> 56.sp
                            ExtendWidget.hugeMode -> 52.sp
                            ExtendWidget.largeMode -> 48.sp
                            ExtendWidget.mediumMode -> 40.sp
                            ExtendWidget.smallMode -> 34.sp
                            else -> 30.sp
                        },
                    ),
                    noTint = true,
                )

                if (showMonth) {
                    Spacer(GlanceModifier.height(20.dp))

                    CanvasText(
                        text = context.resources.getString(R.string.this_month),
                        style = TextStyle(
                            color = contentColor,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                        ),
                        noTint = true,
                    )

                    Spacer(GlanceModifier.height(4.dp))

                    CanvasText(
                        text = numberFormat(context, monthSpent, currency),
                        style = TextStyle(
                            color = contentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = when (size) {
                                ExtendWidget.superHugeMode, ExtendWidget.hugeMode -> 26.sp
                                else -> 20.sp
                            },
                        ),
                        noTint = true,
                    )
                }
            }

            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalAlignment = Alignment.End,
            ) {
                val drawable = ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.ic_add,
                    null,
                )!!

                Image(
                    modifier = GlanceModifier.size(
                        when (size) {
                            ExtendWidget.superHugeMode,
                            ExtendWidget.hugeMode,
                            ExtendWidget.largeMode -> 32.dp

                            else -> 24.dp
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
