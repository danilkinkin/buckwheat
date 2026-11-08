package com.danilkinkin.buckwheat.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.*
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.unit.ColorProvider
import androidx.lifecycle.asFlow
import com.danilkinkin.buckwheat.di.SpendsRepository
import com.danilkinkin.buckwheat.util.isSameDay
import com.danilkinkin.buckwheat.util.toLocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.YearMonth
import java.util.Date
import javax.inject.Inject

/**
 * Glance exposes a factory function for a [ColorProvider] backed by an arbitrary Compose
 * [Color]; there is no such constructor. The previous implementation looked one up with
 * reflection, which never worked because `ColorProvider` is an interface - the lookup threw
 * on every call and every colour silently fell back to the ambient content colour.
 */
fun Color.toColorProvider(): ColorProvider = ColorProvider(this)

val LocalContentColor = compositionLocalOf<ColorProvider> { throw Error("No set") }
val LocalAccentColor = compositionLocalOf<ColorProvider> { throw Error("No set") }

/**
 * Shared receiver for both widgets. It only publishes ledger totals: what was spent today and
 * this month, plus the display currency. The budget period no longer exists in the app.
 */
abstract class WidgetReceiver : GlanceAppWidgetReceiver() {

    companion object {
        const val UPDATE_ACTION = "updateAction"

        val todaySpentPreferenceKey = stringPreferencesKey("today-spent-key")
        val monthSpentPreferenceKey = stringPreferencesKey("month-spent-key")
        val currencyPreferenceKey = stringPreferencesKey("currency-key")

        /**
         * Receivers are short lived but the work they start is not. A scope created per
         * instance is never cancelled and its jobs are dropped when the process goes away, so
         * every refresh leaked a scope. One shared scope, together with goAsync() in
         * [onReceive], keeps a refresh alive for as long as the system allows the broadcast to
         * run.
         */
        private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun requestUpdateData(context: Context, receiverClass: Class<*>) {
            val intent = Intent(context, receiverClass)
            intent.action = UPDATE_ACTION
            context.sendBroadcast(intent)
        }
    }

    @Inject
    lateinit var databaseRepository: SpendsRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        receiverScope.launch { observeData(context) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action != UPDATE_ACTION) return

        val pendingResult = goAsync()
        receiverScope.launch {
            try {
                observeData(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun observeData(context: Context) {
        val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(glanceAppWidget.javaClass)

        val spends = databaseRepository.getAllSpends().asFlow().first()
        val currency = databaseRepository.getCurrency().first()

        val today = Date()
        val currentMonth = YearMonth.from(today.toLocalDate())
        val todaySpent = spends
            .filter { isSameDay(it.date, today) }
            .fold(BigDecimal.ZERO) { sum, spend -> sum + spend.value }
        val monthSpent = spends
            .filter { YearMonth.from(it.date.toLocalDate()) == currentMonth }
            .fold(BigDecimal.ZERO) { sum, spend -> sum + spend.value }

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(
                context = context,
                definition = PreferencesGlanceStateDefinition,
                glanceId = glanceId
            ) { preferences ->
                preferences.toMutablePreferences()
                    .apply {
                        this[todaySpentPreferenceKey] = todaySpent.toString()
                        this[monthSpentPreferenceKey] = monthSpent.toString()
                        this[currencyPreferenceKey] = currency.value.toString()
                    }
            }

            glanceAppWidget.update(context, glanceId)
        }
    }
}
