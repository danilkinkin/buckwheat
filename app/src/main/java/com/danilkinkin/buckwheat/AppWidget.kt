package com.danilkinkin.buckwheat


import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.danilkinkin.buckwheat.util.ExtendCurrency
import com.danilkinkin.buckwheat.util.prettyCandyCanes
import com.danilkinkin.buckwheat.widget.AddSpentButton
import com.danilkinkin.buckwheat.widget.Label
import java.math.BigDecimal


class AppWidget : GlanceAppWidget() {
    companion object {
        private val superTinyMode = DpSize(200.dp, 48.dp)
        private val tinyMode = DpSize(200.dp, 84.dp)
        private val smallMode = DpSize(200.dp, 100.dp)
        private val mediumMode = DpSize(200.dp, 130.dp)
        private val largeMode = DpSize(200.dp, 190.dp)
        private val hugeMode = DpSize(200.dp, 320.dp)
        private val superHugeMode = DpSize(200.dp, 460.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(superTinyMode, tinyMode, smallMode, mediumMode, largeMode, hugeMode, superHugeMode)
    )

    @Composable
    @GlanceComposable
    override fun Content() {
        val size = LocalSize.current
        val context = LocalContext.current

        val intent = Intent(context, MainActivity::class.java)
        //actionStartActivity(intent)

        val isTiny = size == superTinyMode || size == tinyMode
        val isHugeAndUpSize = size == hugeMode || size == superHugeMode

        val verticalPadding = if (size == superTinyMode) 0.dp else 12.dp

        Column(
            modifier = GlanceModifier.clickable(actionRunCallback<AddSpendActionCallback>())
                .fillMaxSize().background(R.color.material_dynamic_secondary95).cornerRadius(24.dp)
        ) {
            Box(
                modifier = GlanceModifier.fillMaxWidth()
                    .then(if (isHugeAndUpSize) GlanceModifier.wrapContentHeight() else GlanceModifier.fillMaxHeight())
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxWidth()
                        .then(if (isHugeAndUpSize) GlanceModifier.wrapContentHeight() else GlanceModifier.fillMaxHeight())
                        .padding(12.dp, 0.dp),
                    verticalAlignment = if (isTiny) Alignment.CenterVertically else Alignment.Top
                ) {
                    Text(
                        modifier = GlanceModifier.padding(8.dp, verticalPadding, 8.dp, 0.dp),
                        text = prettyCandyCanes(
                            BigDecimal("10000"), ExtendCurrency.getInstance("USD")
                        ),
                        style = TextStyle(
                            color = ColorProvider(R.color.material_dynamic_primary40),
                            fontWeight = FontWeight.Bold,
                            fontSize = when (size) {
                                superHugeMode -> 56.sp
                                superTinyMode -> 24.sp
                                else -> 36.sp
                            },
                        )
                    )
                    Label(
                        modifier = GlanceModifier.padding(8.dp, 0.dp, 8.dp, verticalPadding + 6.dp),
                        text = context.resources.getString(R.string.budget_for_today),
                    )
                    if (size == largeMode || isHugeAndUpSize) {
                        Text(
                            modifier = GlanceModifier.padding(8.dp, verticalPadding, 8.dp, 0.dp),
                            text = prettyCandyCanes(
                                BigDecimal("698"), ExtendCurrency.getInstance("USD")
                            ),
                            style = TextStyle(
                                color = ColorProvider(R.color.material_dynamic_secondary20),
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                            )
                        )
                        Label(
                            modifier = GlanceModifier.padding(
                                8.dp, 0.dp, 8.dp, verticalPadding + 6.dp
                            ),
                            text = context.resources.getString(R.string.today),
                        )
                    }
                }
                if (!isTiny) {
                    Column(
                        modifier = if (!isHugeAndUpSize) GlanceModifier.fillMaxSize() else GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        AddSpentButton()
                    }
                }
            }
            if (isHugeAndUpSize) {
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(8.dp, 0.dp, 8.dp, 8.dp)
                ) {
                    Column(
                        modifier = GlanceModifier
                            .then(if (size == superHugeMode) GlanceModifier.fillMaxWidth().height(120.dp) else GlanceModifier.fillMaxSize())
                            .cornerRadius(20.dp)
                            .background(R.color.material_dynamic_secondary90)
                    ) {
                        Text("History")
                    }

                    if (size == superHugeMode) {
                        Spacer(GlanceModifier.height(8.dp))
                        Column(
                            modifier = GlanceModifier
                                .then(if (size == superHugeMode) GlanceModifier.fillMaxWidth().defaultWeight() else GlanceModifier.fillMaxSize())
                                .cornerRadius(20.dp)
                                .background(R.color.material_dynamic_secondary90)
                        ) {
                            Text("History")
                        }
                    }
                }
            }
        }
    }
}


class AddSpendActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context, glanceId: GlanceId, parameters: ActionParameters
    ) {
        Log.d("Buckwheat widget", "add spend...")
    }
}

class AppWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = AppWidget()
}