package com.danilkinkin.buckwheat.widget.minimal

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
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import com.danilkinkin.buckwheat.MainActivity
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.util.ExtendCurrency
import com.danilkinkin.buckwheat.util.prettyCandyCanes
import com.danilkinkin.buckwheat.widget.BuckwheatWidgetTheme
import com.danilkinkin.buckwheat.widget.CanvasText
import com.danilkinkin.buckwheat.widget.WidgetReceiver
import java.math.BigDecimal

@Composable
@GlanceComposable
fun WidgetContent() {
    val size = LocalSize.current
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java)

    val prefs = currentState<Preferences>()
    val todayBudget = prefs[WidgetReceiver.todayBudgetPreferenceKey] ?: 0
    val currency = prefs[WidgetReceiver.currencyPreferenceKey]
    val stateBudget =
        WidgetReceiver.Companion.StateBudget.valueOf(
            prefs[WidgetReceiver.stateBudgetPreferenceKey]
                ?: WidgetReceiver.Companion.StateBudget.NOT_SET.name
        )
    val spentPercent = prefs[WidgetReceiver.spentPercentPreferenceKey] ?: 0F

    BuckwheatWidgetTheme {
        Box(
            modifier = GlanceModifier
                .cornerRadius(48.dp)
                .fillMaxSize()
                .background(GlanceTheme.colors.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = GlanceModifier.padding(24.dp),
                horizontalAlignment = Alignment.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (
                    stateBudget !== WidgetReceiver.Companion.StateBudget.NOT_SET &&
                    stateBudget !== WidgetReceiver.Companion.StateBudget.END_PERIOD
                ) {
                    Row(
                        modifier = GlanceModifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CanvasText(
                            modifier = GlanceModifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                            text = context.resources.getString(
                                R.string.add_spent
                            ),
                            style = TextStyle(
                                color = GlanceTheme.colors.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                            )
                        )

                        val drawable = ResourcesCompat.getDrawable(
                            context.resources,
                            R.drawable.ic_add,
                            null,
                        )!!

                        Image(
                            modifier = GlanceModifier.size(32.dp),
                            provider = ImageProvider(drawable.toBitmap()),
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                            contentDescription = null,
                        )
                    }
                }
                if (
                    stateBudget === WidgetReceiver.Companion.StateBudget.NOT_SET ||
                    stateBudget === WidgetReceiver.Companion.StateBudget.END_PERIOD
                ) {
                    CanvasText(
                        modifier = GlanceModifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                        text = if (stateBudget === WidgetReceiver.Companion.StateBudget.NOT_SET) {
                            context.resources.getString(
                                R.string.budget_not_set
                            )
                        } else {
                            context.resources.getString(
                                R.string.finish_period_title
                            )
                        },
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        )
                    )

                    Row(
                        modifier = GlanceModifier.padding(0.dp, 4.dp, 0.dp, 0.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CanvasText(
                            modifier = GlanceModifier.padding(0.dp, 0.dp, 6.dp, 0.dp),
                            text = context.resources.getString(
                                R.string.set_period_title
                            ),
                            style = TextStyle(
                                color = GlanceTheme.colors.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                            )
                        )

                        val drawable = ResourcesCompat.getDrawable(
                            context.resources,
                            R.drawable.ic_arrow_forward,
                            null,
                        )!!

                        Image(
                            modifier = GlanceModifier.size(22.dp),
                            provider = ImageProvider(drawable.toBitmap()),
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                            contentDescription = null,
                        )
                    }
                }
            }
        }

        Box(
            modifier = GlanceModifier
                .appWidgetBackground()
                .cornerRadius(24.dp)
                .fillMaxSize()
                .clickable(actionStartActivity(intent))
        ) {}
    }
}