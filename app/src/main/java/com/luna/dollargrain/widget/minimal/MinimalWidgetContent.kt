package com.luna.dollargrain.widget.minimal

import android.content.Intent
import androidx.compose.runtime.Composable
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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import com.luna.dollargrain.BuildConfig
import com.luna.dollargrain.R
import com.luna.dollargrain.MainActivity
import com.luna.dollargrain.widget.CanvasText
import com.luna.dollargrain.widget.WidgetReceiver

@Composable
@GlanceComposable
fun MinimalWidgetContent() {
    val size = LocalSize.current
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java)

    val prefs = currentState<Preferences>()
    val stateBudget = WidgetReceiver.StateBudget.valueOf(
        prefs[WidgetReceiver.stateBudgetPreferenceKey]
            ?: WidgetReceiver.StateBudget.NOT_SET.name
    )
    Box(
        modifier = GlanceModifier
            .cornerRadius(48.dp)
            .fillMaxSize()
            .background(ImageProvider(R.drawable.minimal_widget_preview_background)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = GlanceModifier.padding(8.dp),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (
                stateBudget !== WidgetReceiver.StateBudget.NOT_SET &&
                stateBudget !== WidgetReceiver.StateBudget.END_PERIOD
            ) {
                Row(
                    modifier = GlanceModifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CanvasText(
                        modifier = GlanceModifier.padding(
                            0.dp,
                            0.dp,
                            when (size) {
                                MinimalWidget.largeMode -> 8.dp
                                MinimalWidget.smallMode -> 4.dp
                                else -> 6.dp
                            },
                            0.dp,
                        ),
                        text = context.resources.getString(
                            when (size) {
                                MinimalWidget.smallMode -> R.string.add_spent_short
                                else -> R.string.add_spent
                            }

                        ),
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontWeight = FontWeight.Medium,
                            fontSize = when (size) {
                                MinimalWidget.largeMode -> 22.sp
                                MinimalWidget.smallMode -> 14.sp
                                else -> 18.sp
                            },
                        )
                    )

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
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
                        contentDescription = null,
                    )
                }
            }
            if (
                stateBudget === WidgetReceiver.StateBudget.NOT_SET ||
                stateBudget === WidgetReceiver.StateBudget.END_PERIOD
            ) {
                CanvasText(
                    modifier = GlanceModifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                    text = if (stateBudget === WidgetReceiver.StateBudget.NOT_SET) {
                        context.resources.getString(
                            R.string.budget_not_set
                        )
                    } else {
                        context.resources.getString(
                            R.string.finish_period_title
                        )
                    },
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = when (size) {
                            MinimalWidget.largeMode -> 24.sp
                            MinimalWidget.smallMode -> 18.sp
                            else -> 18.sp
                        },
                    )
                )

                Row(
                    modifier = GlanceModifier.padding(
                        0.dp,
                        when (size) {
                            MinimalWidget.largeMode -> 4.dp
                            MinimalWidget.smallMode -> 0.dp
                            else -> 2.dp
                        },
                        0.dp,
                        0.dp,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CanvasText(
                        modifier = GlanceModifier.padding(
                            0.dp,
                            0.dp,
                            when (size) {
                                MinimalWidget.largeMode -> 6.dp
                                MinimalWidget.smallMode -> 2.dp
                                else -> 4.dp
                            },
                            0.dp,
                        ),
                        text = context.resources.getString(
                            R.string.set_period_title
                        ),
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = when (size) {
                                MinimalWidget.largeMode -> 16.sp
                                MinimalWidget.smallMode -> 12.sp
                                else -> 14.sp
                            },
                        )
                    )

                    val drawable = ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ic_arrow_forward,
                        null,
                    )!!

                    Image(
                        modifier = GlanceModifier.size(
                            when (size) {
                                MinimalWidget.largeMode -> 22.dp
                                MinimalWidget.smallMode -> 14.dp
                                else -> 20.dp
                            }
                        ),
                        provider = ImageProvider(drawable.toBitmap()),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.primary),
                        contentDescription = null,
                    )
                }
            }
        }
    }

    if (BuildConfig.DEBUG) {
        Box(
            modifier = GlanceModifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CanvasText(
                modifier = GlanceModifier.padding(top = 8.dp),
                text = "${size.width}x${size.height}", style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                )
            )
        }
    }

    Box(
        modifier = GlanceModifier
            .cornerRadius(48.dp)
            .fillMaxSize()
            .clickable(actionStartActivity(intent))
    ) {}
}