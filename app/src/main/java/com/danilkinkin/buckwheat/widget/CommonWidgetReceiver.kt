package com.danilkinkin.buckwheat.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.*
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.danilkinkin.buckwheat.di.DatabaseRepository
import com.danilkinkin.buckwheat.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    lateinit var databaseRepository: DatabaseRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        Log.d("WidgetReceiver", "onUpdate")

        observeData(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)

        Log.d("WidgetReceiver", "onAppWidgetOptionsChanged")
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        Log.d("WidgetReceiver", "onReceive")

        if (intent.action == UPDATE_ACTION) {
            observeData(context)
        }
    }

    private fun calcBudgetPerDaySplit(
        finishDate: Date,
        spent: BigDecimal,
        budget: BigDecimal,
        dailyBudget: BigDecimal,
        spentFromDailyBudget: BigDecimal,
    ): BigDecimal {
        val restDays = countDays(finishDate) - 1
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

            val storageDao = databaseRepository.storageDao()

            val finishDate: Date? = try {
                Date(storageDao.get("finishDate").value.toLong())
            } catch (e: Exception) {
                null
            }

            val spentFromDailyBudget: BigDecimal = try {
                storageDao.get("spentFromDailyBudget").value.toBigDecimal()
            } catch (e: Exception) {
                0.0.toBigDecimal()
            }

            val dailyBudget: BigDecimal = try {
                storageDao.get("dailyBudget").value.toBigDecimal()
            } catch (e: Exception) {
                0.0.toBigDecimal()
            }

            val spent: BigDecimal = try {
                storageDao.get("spent").value.toBigDecimal()
            } catch (e: Exception) {
                0.0.toBigDecimal()
            }

            val budget: BigDecimal = try {
                storageDao.get("budget").value.toBigDecimal()
            } catch (e: Exception) {
                0.toBigDecimal()
            }

            val currency: ExtendCurrency = try {
                ExtendCurrency.getInstance(storageDao.get("currency").value)
            } catch (e: Exception) {
                ExtendCurrency(value = null, type = CurrencyType.NONE)
            }

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

                val newPerDayBudget = calcBudgetPerDaySplit(
                    finishDate = finishDate,
                    spent = spent,
                    budget = budget,
                    dailyBudget = dailyBudget,
                    spentFromDailyBudget = spentFromDailyBudget,
                )

                val endBudget = newPerDayBudget <= BigDecimal(0)

                val percent =
                    if (dailyBudget > BigDecimal(0)) (dailyBudget - spentFromDailyBudget).divide(
                        dailyBudget,
                        5,
                        RoundingMode.HALF_EVEN
                    ) else BigDecimal(0)

                val finalBudgetValue = if (newBudget >= 0.toBigDecimal()) {
                    newBudget
                } else {
                    newPerDayBudget.coerceAtLeast(BigDecimal(0))
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
                                    if (newBudget >= 0.toBigDecimal()) {
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
