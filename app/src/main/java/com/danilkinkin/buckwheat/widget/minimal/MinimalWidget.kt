package com.danilkinkin.buckwheat.widget.minimal

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import com.danilkinkin.buckwheat.MainActivity
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.util.ExtendCurrency
import com.danilkinkin.buckwheat.util.prettyCandyCanes
import com.danilkinkin.buckwheat.widget.BuckwheatWidgetTheme
import com.danilkinkin.buckwheat.widget.CanvasText
import com.danilkinkin.buckwheat.widget.WidgetReceiver
import com.danilkinkin.buckwheat.widget.WidgetReceiver.Companion.currencyPreferenceKey
import com.danilkinkin.buckwheat.widget.WidgetReceiver.Companion.spentPercentPreferenceKey
import com.danilkinkin.buckwheat.widget.WidgetReceiver.Companion.stateBudgetPreferenceKey
import com.danilkinkin.buckwheat.widget.WidgetReceiver.Companion.todayBudgetPreferenceKey
import java.math.BigDecimal

class MinimalWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    companion object {
        private val superTinyMode = DpSize(200.dp, 48.dp)
        private val tinyMode = DpSize(200.dp, 84.dp)
        private val smallMode = DpSize(200.dp, 100.dp)
        private val mediumMode = DpSize(200.dp, 130.dp)
        private val largeMode = DpSize(200.dp, 190.dp)
        private val hugeMode = DpSize(200.dp, 280.dp)
        private val superHugeMode = DpSize(200.dp, 460.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(superTinyMode, tinyMode, smallMode, mediumMode, largeMode, hugeMode, superHugeMode)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val size = LocalSize.current
            val intent = Intent(context, MainActivity::class.java)

            val prefs = currentState<Preferences>()
            val todayBudget = prefs[todayBudgetPreferenceKey] ?: 0
            val currency = prefs[currencyPreferenceKey]
            val stateBudget =
                WidgetReceiver.Companion.StateBudget.valueOf(prefs[stateBudgetPreferenceKey] ?: WidgetReceiver.Companion.StateBudget.NOT_SET.name)
            val spentPercent = prefs[spentPercentPreferenceKey] ?: 0F

            BuckwheatWidgetTheme {
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(24.dp)
                        .fillMaxSize()
                        .background(GlanceTheme.colors.primaryContainer)
                ) {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        if (
                            stateBudget !== WidgetReceiver.Companion.StateBudget.NOT_SET &&
                            stateBudget !== WidgetReceiver.Companion.StateBudget.END_PERIOD
                        ) {
                            Row(
                                modifier = GlanceModifier.padding(
                                    16.dp, 16.dp, 24.dp, 0.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val drawable = ResourcesCompat.getDrawable(
                                    context.resources,
                                    R.drawable.ic_info,
                                    null,
                                )!!

                                Image(
                                    modifier = GlanceModifier.size(16.dp),
                                    provider = ImageProvider(drawable.toBitmap()),
                                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                                    contentDescription = null,
                                )

                                CanvasText(
                                    modifier = GlanceModifier.padding(
                                        8.dp, 0.dp, 0.dp, 0.dp
                                    ),
                                    text = when (stateBudget) {
                                        WidgetReceiver.Companion.StateBudget.NEW_DAILY -> context.resources.getString(
                                            R.string.rest_budget_for_today
                                        )

                                        WidgetReceiver.Companion.StateBudget.IS_OVER -> context.resources.getString(
                                            R.string.budget_end
                                        )

                                        else -> context.resources.getString(
                                            R.string.rest_budget_for_today
                                        )
                                    },
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                    )
                                )
                            }
                        }
                        Column(
                            modifier = GlanceModifier.defaultWeight().padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (
                                stateBudget !== WidgetReceiver.Companion.StateBudget.NOT_SET &&
                                stateBudget !== WidgetReceiver.Companion.StateBudget.IS_OVER &&
                                stateBudget !== WidgetReceiver.Companion.StateBudget.END_PERIOD
                            ) {
                                CanvasText(
                                    modifier = GlanceModifier.padding(24.dp, 0.dp),
                                    text = prettyCandyCanes(
                                        BigDecimal(todayBudget),
                                        ExtendCurrency.getInstance(currency)
                                    ),
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = when (size) {
                                            superHugeMode -> 56.sp
                                            superTinyMode -> 24.sp
                                            else -> 36.sp
                                        },
                                    )
                                )
                            } else if (stateBudget !== WidgetReceiver.Companion.StateBudget.IS_OVER) {
                                CanvasText(
                                    modifier = GlanceModifier.padding(24.dp, 0.dp),
                                    text = if (stateBudget === WidgetReceiver.Companion.StateBudget.NOT_SET) context.resources.getString(
                                        R.string.budget_not_set
                                    ) else context.resources.getString(R.string.finish_period_title),
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = when (size) {
                                            superHugeMode -> 42.sp
                                            superTinyMode -> 18.sp
                                            else -> 24.sp
                                        },
                                    )
                                )
                            }
                        }
                    }
                    if (stateBudget !== WidgetReceiver.Companion.StateBudget.NOT_SET && stateBudget !== WidgetReceiver.Companion.StateBudget.END_PERIOD) {
                        Column(
                            modifier = GlanceModifier.fillMaxSize(),
                            horizontalAlignment = Alignment.End,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Row(
                                modifier = GlanceModifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val drawable = ResourcesCompat.getDrawable(
                                    context.resources,
                                    R.drawable.ic_add,
                                    null,
                                )!!

                                Image(
                                    modifier = GlanceModifier.size(24.dp),
                                    provider = ImageProvider(drawable.toBitmap()),
                                    colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                    if (stateBudget === WidgetReceiver.Companion.StateBudget.NOT_SET || stateBudget === WidgetReceiver.Companion.StateBudget.END_PERIOD) {
                        Column(
                            modifier = GlanceModifier.fillMaxSize(),
                            horizontalAlignment = Alignment.End,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Row(
                                modifier = GlanceModifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CanvasText(
                                    modifier = GlanceModifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
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
                                    modifier = GlanceModifier.size(24.dp),
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
    }
}