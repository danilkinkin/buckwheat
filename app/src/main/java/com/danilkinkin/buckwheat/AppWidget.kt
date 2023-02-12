package com.danilkinkin.buckwheat

import android.content.Context
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.glance.text.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.danilkinkin.buckwheat.home.MainScreen
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.FontCard
import com.danilkinkin.buckwheat.util.ExtendCurrency
import com.danilkinkin.buckwheat.util.prettyCandyCanes
import java.math.BigDecimal


class AppWidget : GlanceAppWidget() {

    /* companion object {
        private val thinMode = DpSize(120.dp, 120.dp)
        private val smallMode = DpSize(184.dp, 184.dp)
        private val mediumMode = DpSize(260.dp, 200.dp)
        private val largeMode = DpSize(260.dp, 280.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(thinMode, smallMode, mediumMode, largeMode)
    ) */

    @Composable
    @GlanceComposable
    override fun Content() {
        val size = LocalSize.current
        val context = LocalContext.current

        Box(
            modifier = GlanceModifier.fillMaxSize().background(R.color.material_dynamic_neutral90)
                .padding(8.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Text(
                    modifier = GlanceModifier.padding(4.dp),
                    text = prettyCandyCanes(BigDecimal("10000"), ExtendCurrency.getInstance("RUB")),
                    style = TextStyle(
                        color = ColorProvider(R.color.material_dynamic_primary10),
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                    )
                )
                Text(
                    text = context.resources.getString(R.string.budget_for_today),
                    style = TextStyle(
                        color = ColorProvider(R.color.material_dynamic_neutral40),
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                    )
                )
                Box(modifier = GlanceModifier.defaultWeight()) {}
                Row(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                    Button(
                        text = "Add spend",
                        onClick = actionRunCallback<AddSpendActionCallback>(),
                    )
                }

            }
        }
    }
}

class AddSpendActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        Log.d("Buckwheat widget", "add spend...")
    }
}

class AppWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget = AppWidget()
}