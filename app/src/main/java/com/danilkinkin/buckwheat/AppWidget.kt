package com.danilkinkin.buckwheat


import android.content.Context
import android.content.Intent
import android.graphics.*
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
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
import com.danilkinkin.buckwheat.util.*
import com.danilkinkin.buckwheat.widget.CanvasText
import com.danilkinkin.buckwheat.widget.Wave
import com.danilkinkin.buckwheat.widget.generateWidgetColorPalette
import java.math.BigDecimal

class AppWidget : GlanceAppWidget() {
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

    @Composable
    @GlanceComposable
    override fun Content() {
        val size = LocalSize.current
        val context = LocalContext.current

        val intent = Intent(context, MainActivity::class.java)
        //actionStartActivity(intent)

        val harmonizedPalette = generateWidgetColorPalette()

        Box(
            modifier = GlanceModifier
                .cornerRadius(24.dp)
                .fillMaxSize()
                .background(harmonizedPalette.container)
        ) {
            Wave(
                percent = 30F,
                color = harmonizedPalette.main,
            )
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Text(
                    modifier = GlanceModifier.padding(
                        24.dp, 16.dp, 24.dp, 0.dp
                    ),
                    text = context.resources.getString(R.string.budget_for_today),
                    style = TextStyle(
                        color = harmonizedPalette.onContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    )
                )
                Column(
                    modifier = GlanceModifier.defaultWeight().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = GlanceModifier.padding(24.dp, 0.dp),
                        text = prettyCandyCanes(
                            BigDecimal("52130"), ExtendCurrency.getInstance("USD")
                        ),
                        style = TextStyle(
                            color = harmonizedPalette.onContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = when (size) {
                                superHugeMode -> 56.sp
                                superTinyMode -> 24.sp
                                else -> 36.sp
                            },
                        )
                    )
                }
            }
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

                    val iconColor = harmonizedPalette.onContainer.getColor(context).toArgb()
                    drawable.colorFilter = LightingColorFilter(iconColor, iconColor)

                    CanvasText(text = "TEST 1234")

                    Image(
                        modifier = GlanceModifier.size(24.dp),
                        provider = ImageProvider(drawable.toBitmap()),
                        contentDescription = null,
                    )
                }
            }
        }

        Box(
            modifier = GlanceModifier
                .appWidgetBackground()
                .cornerRadius(24.dp)
                .fillMaxSize()
                .clickable(actionRunCallback<AddSpendActionCallback>())
        ){}
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