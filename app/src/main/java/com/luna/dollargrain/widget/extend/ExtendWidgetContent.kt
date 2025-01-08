package com.luna.dollargrain.widget.extend

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
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
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import com.luna.dollargrain.BuildConfig
import com.luna.dollargrain.MainActivity
import com.luna.dollargrain.R
import com.luna.dollargrain.data.ExtendCurrency
import com.luna.dollargrain.util.numberFormat
import com.luna.dollargrain.widget.CanvasText
import com.luna.dollargrain.widget.LocalAccentColor
import com.luna.dollargrain.widget.LocalContentColor
import com.luna.dollargrain.widget.WidgetReceiver
import java.math.BigDecimal
import java.text.BreakIterator

@Composable
@GlanceComposable
fun ExtendWidgetContent() {
    val size = LocalSize.current
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java)

    val prefs = currentState<Preferences>()
    val todayBudget = BigDecimal(prefs[WidgetReceiver.todayBudgetPreferenceKey] ?: "0")
    val currency = prefs[WidgetReceiver.currencyPreferenceKey]
    val stateBudget = WidgetReceiver.StateBudget.valueOf(
        prefs[WidgetReceiver.stateBudgetPreferenceKey]
            ?: WidgetReceiver.StateBudget.NOT_SET.name
    )


    CompositionLocalProvider(
        LocalContentColor provides GlanceTheme.colors.onSurface,
        LocalAccentColor provides GlanceTheme.colors.primary,
    ) {
        val accentColor = LocalAccentColor.current
        val contentColor = LocalContentColor.current

        Box(
            modifier = GlanceModifier
                .cornerRadius(32.dp)
                .fillMaxSize()
                .background(ImageProvider(R.drawable.extend_widget_background))
                .padding(
                    when (size) {
                        ExtendWidget.superHugeMode, ExtendWidget.hugeMode -> 8.dp
                        else -> 0.dp
                    }
                )
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                if (
                    stateBudget !== WidgetReceiver.StateBudget.NOT_SET &&
                    stateBudget !== WidgetReceiver.StateBudget.END_PERIOD
                ) {
                    Row(
                        modifier = GlanceModifier.padding(24.dp, 16.dp, 16.dp, 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (stateBudget === WidgetReceiver.StateBudget.NORMAL) {
                            CanvasText(
                                modifier = GlanceModifier.padding(start = 0.dp, end = 0.dp),
                                text = "for today",
                                style = TextStyle(
                                    color = contentColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = when (size) {
                                        ExtendWidget.superHugeMode,
                                        ExtendWidget.hugeMode,
                                        ExtendWidget.largeMode -> 16.sp

                                        else -> 12.sp
                                    },
                                )
                            )
                        } else {
                            val drawable = ResourcesCompat.getDrawable(
                                context.resources,
                                R.drawable.ic_info,
                                null,
                            )!!

                            Image(
                                modifier = GlanceModifier.size(
                                    when (size) {
                                        ExtendWidget.superHugeMode, ExtendWidget.hugeMode -> 18.dp
                                        else -> 16.dp
                                    }
                                ),
                                provider = ImageProvider(drawable.toBitmap()),
                                colorFilter = ColorFilter.tint(contentColor),
                                contentDescription = null,
                            )

                            CanvasText(
                                modifier = GlanceModifier.padding(8.dp, 0.dp, 0.dp, 0.dp),
                                text =
                                    if (stateBudget === WidgetReceiver.StateBudget.NEW_DAILY) {
                                        "new daily"
                                    } else {
                                        "budget over"
                                    },
                                style = TextStyle(
                                    color = contentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = when (size) {
                                        ExtendWidget.superHugeMode, ExtendWidget.hugeMode -> 16.sp
                                        else -> 12.sp
                                    },
                                )
                            )
                        }

                    }
                }
                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .padding(
                            24.dp,
                            when (size) {
                                ExtendWidget.smallMode -> (-12).dp
                                ExtendWidget.tinyMode -> (-8).dp
                                else -> 0.dp
                            },
                            0.dp,
                            when (size) {
                                ExtendWidget.smallMode -> 4.dp
                                ExtendWidget.tinyMode -> 0.dp
                                else -> 32.dp
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start,
                ) {
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight(),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row {
                            if (
                                stateBudget !== WidgetReceiver.StateBudget.NOT_SET &&
                                stateBudget !== WidgetReceiver.StateBudget.IS_OVER &&
                                stateBudget !== WidgetReceiver.StateBudget.END_PERIOD
                            ) {
                                val splittedValue = emptyList<String>().toMutableList()
                                val value = numberFormat(
                                    context,
                                    todayBudget,
                                    ExtendCurrency.getInstance(currency),
                                )
                                val it = BreakIterator.getCharacterInstance()
                                it.setText(value)

                                var start = 0
                                var pevEnd = 0
                                var end = it.next()
                                var prevIsSurrogate = false
                                do {
                                    val isSurrogate = value
                                        .substring(pevEnd, end)
                                        .toCharArray()
                                        .any { char -> char.isSurrogate() }

                                    if (prevIsSurrogate != isSurrogate) {
                                        splittedValue.add(value.substring(start, pevEnd))
                                        start = pevEnd
                                    }

                                    pevEnd = end
                                    end = it.next()
                                    prevIsSurrogate = isSurrogate
                                } while (end != BreakIterator.DONE)
                                splittedValue.add(value.substring(start, pevEnd))

                                splittedValue.forEach {
                                    CanvasText(
                                        text = it,
                                        style = TextStyle(
                                            color = contentColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = when (size) {
                                                ExtendWidget.superHugeMode -> 56.sp
                                                ExtendWidget.hugeMode -> 52.sp
                                                ExtendWidget.smallMode -> 32.sp
                                                ExtendWidget.tinyMode -> 24.sp
                                                else -> 42.sp
                                            },
                                        ),
                                        noTint = it
                                            .toCharArray()
                                            .any { char -> char.isSurrogate() },
                                    )
                                }
                            } else if (stateBudget !== WidgetReceiver.StateBudget.IS_OVER) {
                                CanvasText(
                                    text =
                                        if (stateBudget === WidgetReceiver.StateBudget.NOT_SET) {
                                            "budget not set"
                                        } else "period ended!",
                                    style = TextStyle(
                                        color = contentColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = when (size) {
                                            ExtendWidget.superHugeMode -> 42.sp
                                            ExtendWidget.hugeMode -> 36.sp
                                            ExtendWidget.tinyMode -> 18.sp
                                            else -> 24.sp
                                        },
                                    )
                                )
                            }

                        }
                        Row(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .padding(0.dp, 14.dp)
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.End,
                        ) {
                            Image(
                                modifier = GlanceModifier.fillMaxHeight().width(36.dp),
                                provider = ImageProvider(R.drawable.extend_widget_gradient),
                                contentDescription = null,
                            )
                            Box(
                                modifier = GlanceModifier
                                    .background(R.color.surface)
                                    .fillMaxHeight()
                                    .width(
                                        when (size) {
                                            ExtendWidget.smallMode -> 48.dp
                                            ExtendWidget.tinyMode -> 48.dp
                                            else -> 16.dp
                                        }
                                    ),
                            ) {

                            }
                        }
                    }
                }
            }
            if (
                stateBudget !== WidgetReceiver.StateBudget.NOT_SET &&
                stateBudget !== WidgetReceiver.StateBudget.END_PERIOD
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.End,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Row(
                        modifier = GlanceModifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
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
                            colorFilter = ColorFilter.tint(
                                accentColor
                            ),
                            contentDescription = null,
                        )
                    }
                }
            }
            if (
                stateBudget === WidgetReceiver.StateBudget.NOT_SET ||
                stateBudget === WidgetReceiver.StateBudget.END_PERIOD
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.End,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Row(
                        modifier = GlanceModifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CanvasText(
                            modifier = GlanceModifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                            text = "set up a budget",
                            style = TextStyle(
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = when (size) {
                                    ExtendWidget.superHugeMode, ExtendWidget.hugeMode -> 18.sp
                                    else -> 16.sp
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
                                    ExtendWidget.superHugeMode, ExtendWidget.hugeMode -> 32.dp
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
    }

    if (BuildConfig.DEBUG) {
        Box(
            modifier = GlanceModifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            CanvasText(
                modifier = GlanceModifier.padding(top = 8.dp),
                text = "${size.width}x${size.height}",
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                )
            )
        }
    }

    Box(
        modifier = GlanceModifier
            .cornerRadius(32.dp)
            .fillMaxSize()
            .clickable(actionStartActivity(intent))
    ) {}
}

@Preview
@Composable
private fun Preview() {
    GlanceTheme {
        ExtendWidgetContent()
    }
}
