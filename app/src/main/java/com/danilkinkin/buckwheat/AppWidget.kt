package com.danilkinkin.buckwheat


import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import javax.inject.Inject

private enum class StateBudget {
    NOT_SET,
    NORMAL,
    NEW_DAILY,
    IS_OVER,
}

private val todayBudgetPreferenceKey = intPreferencesKey("today-budget-key")
private val currencyPreferenceKey = stringPreferencesKey("currency-key")
private val stateBudgetPreferenceKey = stringPreferencesKey("state-budget-key")
private val spentPercentPreferenceKey = floatPreferencesKey("spent-percent-key")

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
            val todayBudget = prefs[todayBudgetPreferenceKey] ?: 0
            val currency = prefs[currencyPreferenceKey]
            val stateBudget =
                StateBudget.valueOf(prefs[stateBudgetPreferenceKey] ?: StateBudget.NOT_SET.name)
            val spentPercent = prefs[spentPercentPreferenceKey] ?: 0F

            BuckwheatWidgetTheme {
                Box(
                    modifier = GlanceModifier
                        .cornerRadius(24.dp)
                        .fillMaxSize()
                        .background(GlanceTheme.colors.primaryContainer)
                ) {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        CanvasText(
                            modifier = GlanceModifier.padding(
                                24.dp, 16.dp, 24.dp, 0.dp
                            ),
                            text = when (stateBudget) {
                                StateBudget.NOT_SET -> context.resources.getString(R.string.budget_for_today)
                                StateBudget.NORMAL -> context.resources.getString(R.string.rest_budget_for_today)
                                StateBudget.NEW_DAILY -> context.resources.getString(R.string.new_daily_budget)
                                StateBudget.IS_OVER -> context.resources.getString(R.string.budget_end)
                            },
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
                                    BigDecimal(todayBudget), ExtendCurrency.getInstance(currency)
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

        observeData(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

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

            val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(AppWidget::class.java)

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

            if (finishDate === null) {
                glanceIds.forEach { glanceId ->
                    updateAppWidgetState(
                        context = context,
                        definition = PreferencesGlanceStateDefinition,
                        glanceId = glanceId
                    ) { preferences ->
                        preferences.toMutablePreferences()
                            .apply {
                                this[stateBudgetPreferenceKey] = StateBudget.NOT_SET.name
                            }
                    }

                    AppWidget().update(context, glanceId)
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
                                this[todayBudgetPreferenceKey] = finalBudgetValue.toInt()
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

                    AppWidget().update(context, glanceId)
                }
            }

        }
    }
}
