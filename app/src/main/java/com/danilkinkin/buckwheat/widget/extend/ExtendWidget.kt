package com.danilkinkin.buckwheat.widget.extend

import android.content.Context
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Box
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition

class ExtendWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    companion object {
        val tinyMode = DpSize(200.dp, 84.dp)
        val smallMode = DpSize(200.dp, 100.dp)
        val mediumMode = DpSize(200.dp, 130.dp)
        val largeMode = DpSize(200.dp, 190.dp)
        val hugeMode = DpSize(200.dp, 280.dp)
        val superHugeMode = DpSize(200.dp, 460.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(tinyMode, smallMode, mediumMode, largeMode, hugeMode, superHugeMode)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                Box(GlanceModifier.appWidgetBackground().cornerRadius(32.dp)){
                    ExtendWidgetContent()
                }
            }
        }
    }
}