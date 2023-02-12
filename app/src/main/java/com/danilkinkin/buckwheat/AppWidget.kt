package com.danilkinkin.buckwheat


import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.LightingColorFilter
import android.util.Log
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
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
import androidx.glance.appwidget.action.actionStartActivity
import java.math.BigDecimal


class AppWidget : GlanceAppWidget() {
    companion object {
        private val superTinyMode = DpSize(200.dp, 48.dp)
        private val tinyMode = DpSize(200.dp, 84.dp)
        private val smallMode = DpSize(200.dp, 100.dp)
        private val mediumMode = DpSize(200.dp, 130.dp)
        private val largeMode = DpSize(200.dp, 190.dp)
        private val hugeMode = DpSize(200.dp, 420.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(superTinyMode, tinyMode, smallMode, mediumMode, largeMode, hugeMode)
    )

    @Composable
    @GlanceComposable
    override fun Content() {
        val size = LocalSize.current
        val context = LocalContext.current

        val intent = Intent(context, MainActivity::class.java)
        //actionStartActivity(intent)

        val isTiny = size == superTinyMode || size == tinyMode

        val verticalPadding = if (size == superTinyMode) 0.dp else 12.dp

        Box(
            modifier = GlanceModifier
                .clickable(actionRunCallback<AddSpendActionCallback>())
                .fillMaxSize()
                .background(R.color.material_dynamic_neutral99)
                .cornerRadius(24.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize().padding(12.dp, 0.dp),
                verticalAlignment = if (isTiny) Alignment.CenterVertically else Alignment.Top
            ) {
                Text(
                    modifier = GlanceModifier.padding(8.dp, verticalPadding, 8.dp, 0.dp),
                    text = prettyCandyCanes(BigDecimal("10000"), ExtendCurrency.getInstance("USD")),
                    style = TextStyle(
                        color = ColorProvider(R.color.material_dynamic_primary10),
                        fontWeight = FontWeight.Bold,
                        fontSize = when (size) {
                            hugeMode -> 56.sp
                            superTinyMode -> 24.sp
                            else -> 36.sp
                        },
                    )
                )
                Text(
                    modifier = GlanceModifier.padding(8.dp, 0.dp, 8.dp, verticalPadding + 6.dp),
                    text = context.resources.getString(R.string.budget_for_today),
                    style = TextStyle(
                        color = ColorProvider(R.color.material_dynamic_neutral40),
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                    )
                )
                if (size == largeMode || size == hugeMode) {
                    Text(
                        modifier = GlanceModifier.padding(8.dp, verticalPadding, 8.dp, 0.dp),
                        text = prettyCandyCanes(BigDecimal("698"), ExtendCurrency.getInstance("USD")),
                        style = TextStyle(
                            color = ColorProvider(R.color.material_dynamic_primary10),
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                        )
                    )
                    Text(
                        modifier = GlanceModifier.padding(8.dp, 0.dp, 8.dp, verticalPadding + 6.dp),
                        text = context.resources.getString(R.string.today),
                        style = TextStyle(
                            color = ColorProvider(R.color.material_dynamic_neutral40),
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                        )
                    )
                }
            }
            if (!isTiny) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.End,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    AddSpentButton()
                }
            }
        }
    }
}

@Composable
fun AddSpentButton() {
    val context = LocalContext.current
    val color = R.color.material_dynamic_primary40

    Row(
        modifier = GlanceModifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = context.resources.getString(R.string.add_spent),
            style = TextStyle(
                color = ColorProvider(color),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        )
        Spacer(modifier = GlanceModifier.width(8.dp))

        val drawable = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.ic_add,
            null,
        )!!

        val iconColor = ContextCompat.getColor(context, color)
        drawable.colorFilter = LightingColorFilter(iconColor, iconColor)

        Image(
            modifier = GlanceModifier.size(24.dp),
            provider = ImageProvider(drawable.toBitmap()),
            contentDescription = null,
        )
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