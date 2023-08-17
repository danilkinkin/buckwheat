package com.danilkinkin.buckwheat.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.*
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
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

abstract class WidgetReceiver : GlanceAppWidgetReceiver() {

    companion object {
        const val UPDATE_ACTION = "updateAction"

        enum class StateBudget {
            NOT_SET,
            END_PERIOD,
            NORMAL,
            NEW_DAILY,
            IS_OVER,
        }

        val todayBudgetPreferenceKey = stringPreferencesKey("today-budget-key")
        val currencyPreferenceKey = stringPreferencesKey("currency-key")
        val stateBudgetPreferenceKey = stringPreferencesKey("state-budget-key")
        val spentPercentPreferenceKey = floatPreferencesKey("spent-percent-key")

        fun requestUpdateData(context: Context, receiverClass: Class<*>) {
            val intent = Intent(context, receiverClass)
            intent.action = UPDATE_ACTION
            context.sendBroadcast(intent)
        }
    }

    private val job = SupervisorJob()
    val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var databaseRepository: SpendsRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        observeData(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == UPDATE_ACTION) {
            observeData(context)
        }
    }

    private fun whatBudgetForDay(
        finishDate: Date,
        spent: BigDecimal,
        budget: BigDecimal,
        dailyBudget: BigDecimal,
        spentFromDailyBudget: BigDecimal,
    ): BigDecimal {
        val restDays = countDaysToToday(finishDate) - 1
        val restBudget = (budget - spent) - dailyBudget
        val splitBudget = restBudget + dailyBudget - spentFromDailyBudget

        return (splitBudget / restDays.toBigDecimal().coerceAtLeast(BigDecimal(1)))
            .setScale(
                0,
                RoundingMode.FLOOR
            )
    }

    private fun observeData(context: Context) {
        coroutineScope.launch {

            val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(glanceAppWidget.javaClass)

            val finishDate: Date? = databaseRepository.getFinishPeriodDate().first()
            val spentFromDailyBudget: BigDecimal = databaseRepository.getSpentFromDailyBudget().first()
            val dailyBudget: BigDecimal = databaseRepository.getDailyBudget().first()
            val spent: BigDecimal = databaseRepository.getSpent().first()
            val budget: BigDecimal = databaseRepository.getBudget().first()
            val currency: ExtendCurrency = databaseRepository.getCurrency().first()

            if (finishDate === null || finishDate.time <= Date().time) {
                glanceIds.forEach { glanceId ->
                    updateAppWidgetState(
                        context = context,
                        definition = PreferencesGlanceStateDefinition,
                        glanceId = glanceId
                    ) { preferences ->
                        preferences.toMutablePreferences()
                            .apply {
                                this[stateBudgetPreferenceKey] =
                                    if (finishDate !== null && finishDate.time <= Date().time) {
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
}
