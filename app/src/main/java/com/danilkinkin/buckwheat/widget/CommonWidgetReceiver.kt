package com.danilkinkin.buckwheat.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.*
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.unit.ColorProvider
import com.danilkinkin.buckwheat.data.BudgetCalculator
import com.danilkinkin.buckwheat.di.SpendsRepository
import com.danilkinkin.buckwheat.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
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

abstract class WidgetReceiver : GlanceAppWidgetReceiver() {

    enum class StateBudget {
        NOT_SET,
        END_PERIOD,
        NORMAL,
        NEW_DAILY,
        IS_OVER,
    }

    companion object {
        const val UPDATE_ACTION = "updateAction"

        val todayBudgetPreferenceKey = stringPreferencesKey("today-budget-key")
        val currencyPreferenceKey = stringPreferencesKey("currency-key")
        val stateBudgetPreferenceKey = stringPreferencesKey("state-budget-key")
        val spentPercentPreferenceKey = floatPreferencesKey("spent-percent-key")

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

    /**
     * The widget shows whole currency units, so the shared result is floored to 0 decimals.
     * The formula itself comes from [BudgetCalculator]: it used to be a second copy here with a
     * different rounding mode, which let the widget disagree with the app about the same day.
     */
    private fun whatBudgetForDay(
        finishDate: Date,
        spent: BigDecimal,
        budget: BigDecimal,
        dailyBudget: BigDecimal,
        spentFromDailyBudget: BigDecimal,
    ): BigDecimal = BudgetCalculator.budgetForDay(
        budget = budget,
        spent = spent,
        dailyBudget = dailyBudget,
        spentFromDailyBudget = spentFromDailyBudget,
        restDays = countDaysToToday(finishDate),
        excludeCurrentDay = true,
        applyTodaySpends = true,
    ).setScale(0, RoundingMode.FLOOR)

    private suspend fun observeData(context: Context) {
        val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(glanceAppWidget.javaClass)

        val finishDate = databaseRepository.getFinishPeriodDate().first()
        val actualFinishDate = databaseRepository.getFinishPeriodActualDate().first()
        val spentFromDailyBudget = databaseRepository.getSpentFromDailyBudget().first()
        val dailyBudget = databaseRepository.getDailyBudget().first()
        val spent = databaseRepository.getSpent().first()
        val budget = databaseRepository.getBudget().first()
        val currency = databaseRepository.getCurrency().first()

        val finishDateReached = finishDate != null && finishDate.time <= Date().time
        val earlyFinishDateReached = actualFinishDate != null && actualFinishDate.time <= Date().time

        if (
            finishDate == null
            || finishDateReached
            || earlyFinishDateReached
        ) {
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(
                    context = context,
                    definition = PreferencesGlanceStateDefinition,
                    glanceId = glanceId
                ) { preferences ->
                    preferences.toMutablePreferences()
                        .apply {
                            this[stateBudgetPreferenceKey] =
                                if (finishDateReached || earlyFinishDateReached) {
                                    StateBudget.END_PERIOD.name
                                } else {
                                    StateBudget.NOT_SET.name
                                }
                        }
                }

                glanceAppWidget.update(context, glanceId)
            }
        } else {
            val newBudget = dailyBudget - spentFromDailyBudget

            val newPerDayBudget = whatBudgetForDay(
                finishDate = finishDate,
                spent = spent,
                budget = budget,
                dailyBudget = dailyBudget,
                spentFromDailyBudget = spentFromDailyBudget,
            )

            val endBudget = newPerDayBudget <= BigDecimal.ZERO

            val percent =
                if (dailyBudget > BigDecimal.ZERO) (dailyBudget - spentFromDailyBudget).divide(
                    dailyBudget,
                    5,
                    RoundingMode.HALF_EVEN
                ) else BigDecimal.ZERO

            val finalBudgetValue = if (newBudget >= BigDecimal.ZERO) {
                newBudget
            } else {
                newPerDayBudget.coerceAtLeast(BigDecimal.ZERO)
            }

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(
                    context = context,
                    definition = PreferencesGlanceStateDefinition,
                    glanceId = glanceId
                ) { preferences ->
                    preferences.toMutablePreferences()
                        .apply {
                            this[todayBudgetPreferenceKey] = finalBudgetValue.toString()
                            this[currencyPreferenceKey] = currency.value.toString()
                            this[stateBudgetPreferenceKey] =
                                if (newBudget >= BigDecimal.ZERO) {
                                    StateBudget.NORMAL.name
                                } else if (endBudget) {
                                    StateBudget.IS_OVER.name
                                } else {
                                    StateBudget.NEW_DAILY.name
                                }
                            this[spentPercentPreferenceKey] = percent.toFloat()
                        }
                }

                glanceAppWidget.update(context, glanceId)
            }
        }
    }
}
