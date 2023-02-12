package com.danilkinkin.buckwheat


import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.*
import android.graphics.Paint.Align
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.unit.ColorProvider
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.danilkinkin.buckwheat.ui.colorBad
import com.danilkinkin.buckwheat.ui.colorGood
import com.danilkinkin.buckwheat.ui.colorNotGood
import com.danilkinkin.buckwheat.util.*
import java.math.BigDecimal
import java.util.*


val Context.isNightMode: Boolean
    get() =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES

data class HarmonizedColorProviderPalette(
    val main: ColorProvider,
    val onMain: ColorProvider,
    val container: ColorProvider,
    val onContainer: ColorProvider,
    val surface: ColorProvider,
    val onSurface: ColorProvider,
    val surfaceVariant: ColorProvider,
    val onSurfaceVariant: ColorProvider,
)

fun buildUpdate(context: Context, time: String): Bitmap {
    val myBitmap: Bitmap = Bitmap.createBitmap(160, 84, Bitmap.Config.ARGB_4444)
    val myCanvas = Canvas(myBitmap)
    val paint = Paint()
    val clock: Typeface = ResourcesCompat.getFont(context, R.font.manrope_bold)!!
    paint.isAntiAlias = true
    paint.isSubpixelText = true
    paint.typeface = clock
    paint.style = Paint.Style.FILL
    paint.color = if (context.isNightMode) Color.Black.toArgb() else Color.White.toArgb()
    paint.textSize = 65F
    paint.textAlign = Align.CENTER
    myCanvas.drawText(time, 80F, 60F, paint)
    return myBitmap
}

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

        val harmonizedColor = harmonize(
            combineColors(
                listOf(
                    colorBad,
                    colorNotGood,
                    colorGood,
                ),
                0.3f,
            ),
            Color(ContextCompat.getColor(context, R.color.material_dynamic_primary40))
        )

        val dayPalette = toPalette(harmonizedColor, darkTheme = false)
        val nightPalette = toPalette(harmonizedColor, darkTheme = true)

        val harmonizedPalette = HarmonizedColorProviderPalette(
            main = ColorProvider(dayPalette.main, nightPalette.main),
            onMain = ColorProvider(dayPalette.onMain, nightPalette.onMain),
            container = ColorProvider(dayPalette.container, nightPalette.container),
            onContainer = ColorProvider(dayPalette.onContainer, nightPalette.onContainer),
            surface = ColorProvider(dayPalette.surface, nightPalette.surface),
            onSurface = ColorProvider(dayPalette.onSurface, nightPalette.onSurface),
            surfaceVariant = ColorProvider(dayPalette.surfaceVariant, nightPalette.surfaceVariant),
            onSurfaceVariant = ColorProvider(
                dayPalette.onSurfaceVariant,
                nightPalette.onSurfaceVariant
            ),
        )

        Box(
            modifier = GlanceModifier.appWidgetBackground().cornerRadius(24.dp).fillMaxSize()
                .background(harmonizedPalette.container)
                .clickable(actionRunCallback<AddSpendActionCallback>())
        ) {
            Row(
                modifier = GlanceModifier

            ) {
                Box(
                    modifier = GlanceModifier.fillMaxHeight().width(60.dp)
                        .background(harmonizedPalette.main)
                ) {}

                val waveDrawable = ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.wave,
                    null,
                )!!

                val waveColor = harmonizedPalette.main.getColor(context).toArgb()
                waveDrawable.colorFilter = LightingColorFilter(waveColor, waveColor)

                Box(
                    modifier = GlanceModifier.fillMaxHeight().background(harmonizedPalette.main)
                        .background(
                            ImageProvider(waveDrawable.toBitmap()), contentScale = ContentScale.Crop
                        )
                ) {

                }
            }
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Text(
                    modifier = GlanceModifier.padding(
                        24.dp, 16.dp, 24.dp, 0.dp
                    ),
                    text = context.resources.getString(R.string.budget_for_today),
                    style = TextStyle(
                        color = ColorProvider(
                            dayPalette.onContainer.copy(alpha = 0.6f),
                            nightPalette.onContainer.copy(alpha = 0.6f)
                        ),
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

                    Image(
                        provider = ImageProvider(buildUpdate(context, "TEST 1234")),
                        contentDescription = null,
                    )

                    Image(
                        modifier = GlanceModifier.size(24.dp),
                        provider = ImageProvider(drawable.toBitmap()),
                        contentDescription = null,
                    )
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
