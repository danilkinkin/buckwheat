package com.danilkinkin.buckwheat


import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.*
import androidx.glance.ColorFilter
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import com.danilkinkin.buckwheat.di.DatabaseRepository
import com.danilkinkin.buckwheat.util.*
import com.danilkinkin.buckwheat.widget.BuckwheatWidgetTheme
import com.danilkinkin.buckwheat.widget.CanvasText
import com.danilkinkin.buckwheat.widget.Wave
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

private val countPreferenceKey = intPreferencesKey("count-key")

class AppWidget : GlanceAppWidget() {

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
            val count = prefs[countPreferenceKey] ?: 0

            BuckwheatWidgetTheme {
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(24.dp)
                        .fillMaxSize()
                        .background(GlanceTheme.colors.primaryContainer)
                ) {
                    Wave(
                        percent = 30F,
                        color = GlanceTheme.colors.primary,
                    )
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        CanvasText(
                            modifier = GlanceModifier.padding(
                                24.dp, 16.dp, 24.dp, 0.dp
                            ),
                            text = context.resources.getString(R.string.budget_for_today),
                            style = TextStyle(
                                color = GlanceTheme.colors.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                            )
                        )
                        Column(
                            modifier = GlanceModifier.defaultWeight().padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CanvasText(
                                modifier = GlanceModifier.padding(24.dp, 0.dp),
                                text = prettyCandyCanes(
                                    BigDecimal(count), ExtendCurrency.getInstance("USD")
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

                            Image(
                                modifier = GlanceModifier.size(24.dp),
                                provider = ImageProvider(drawable.toBitmap()),
                                colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
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
                        .clickable(actionStartActivity(intent))
                ) {}
            }
        }
    }
}

@AndroidEntryPoint
class AppWidgetReceiver : GlanceAppWidgetReceiver() {

    companion object {
        const val UPDATE_ACTION = "updateAction"

        fun requestUpdateData(context: Context) {
            val intent = Intent(context, AppWidgetReceiver::class.java)
            intent.action = UPDATE_ACTION
            context.sendBroadcast(intent)
        }
    }

    override val glanceAppWidget = AppWidget()


    private val job = SupervisorJob()
    val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var databaseRepository: DatabaseRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d("AppWidgetReceiver", "onUpdate")

        observeData(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.d("AppWidgetReceiver", "onReceive")

        if (intent.action == UPDATE_ACTION) {
            observeData(context)
        }
    }

    private fun observeData(context: Context) {
        coroutineScope.launch {

            val spends = databaseRepository.spentDao().getAllSync()

            Log.d("AppWidgetReceiver", "observeData spends: ${spends.size}")

            val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(AppWidget::class.java)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(
                    context = context,
                    definition = PreferencesGlanceStateDefinition,
                    glanceId = glanceId
                ) { preferences ->
                    preferences.toMutablePreferences()
                        .apply {
                            this[countPreferenceKey] = spends.size
                        }
                }

                AppWidget().update(context, glanceId)
            }

        }
    }
}
