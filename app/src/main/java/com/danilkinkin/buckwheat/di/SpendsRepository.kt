package com.danilkinkin.buckwheat.di

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.LiveData
import com.danilkinkin.buckwheat.budgetDataStore
import com.danilkinkin.buckwheat.data.RestedBudgetDistributionMethod
import com.danilkinkin.buckwheat.data.dao.SpentDao
import com.danilkinkin.buckwheat.data.entities.Spent
import com.danilkinkin.buckwheat.util.CurrencyType
import com.danilkinkin.buckwheat.util.DAY
import com.danilkinkin.buckwheat.util.ExtendCurrency
import com.danilkinkin.buckwheat.util.countDays
import com.danilkinkin.buckwheat.util.countDaysToToday
import com.danilkinkin.buckwheat.util.isToday
import com.danilkinkin.buckwheat.util.roundToDay
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import javax.inject.Inject
import kotlin.math.abs

val currencyStoreKey = stringPreferencesKey("currency")
val restedBudgetDistributionMethodStoreKey = stringPreferencesKey("restedBudgetDistributionMethod")
val hideOverspendingWarnStoreKey = booleanPreferencesKey("hideOverspendingWarn")

val budgetStoreKey = stringPreferencesKey("budget")
val spentStoreKey = stringPreferencesKey("spent")
val dailyBudgetStoreKey = stringPreferencesKey("dailyBudget")
val spentFromDailyBudgetStoreKey = stringPreferencesKey("spentFromDailyBudget")
val lastChangeDailyBudgetDateStoreKey = longPreferencesKey("lastChangeDailyBudgetDate")
val startPeriodDateStoreKey = longPreferencesKey("startPeriodDate")
val finishPeriodDateStoreKey = longPreferencesKey("finishPeriodDate")

class SpendsRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val spentDao: SpentDao,
){
    fun getAllSpends(): LiveData<List<Spent>> = spentDao.getAll()
    fun getBudget() = context.budgetDataStore.data.map {
        it[budgetStoreKey]?.toBigDecimal() ?: BigDecimal.ZERO
    }
    fun getSpent() = context.budgetDataStore.data.map {
        it[spentStoreKey]?.toBigDecimal() ?: BigDecimal.ZERO
    }
    fun getDailyBudget() = context.budgetDataStore.data.map {
        it[dailyBudgetStoreKey]?.toBigDecimal() ?: BigDecimal.ZERO
    }
    fun getSpentFromDailyBudget() = context.budgetDataStore.data.map {
        it[spentFromDailyBudgetStoreKey]?.toBigDecimal() ?: BigDecimal.ZERO
    }
    fun getStartPeriodDate() = context.budgetDataStore.data.map { it ->
        it[startPeriodDateStoreKey]?.let { value -> Date(value) } ?: Date()
    }
    fun getFinishPeriodDate() = context.budgetDataStore.data.map {
        it[finishPeriodDateStoreKey]?.let { value -> Date(value) }
    }
    fun getLastChangeDailyBudgetDate() = context.budgetDataStore.data.map {
        it[lastChangeDailyBudgetDateStoreKey]?.let { value -> Date(value) }
    }

    fun getCurrency() = context.budgetDataStore.data.map {
        it[currencyStoreKey]?.let { value ->
            ExtendCurrency(value = value, type = CurrencyType.NONE)
        } ?: ExtendCurrency(value = null, type = CurrencyType.NONE)
    }
    fun getRestedBudgetDistributionMethod() = context.budgetDataStore.data.map { it ->
        it[restedBudgetDistributionMethodStoreKey]?.let {
            RestedBudgetDistributionMethod.valueOf(it)
        } ?: RestedBudgetDistributionMethod.ASK
    }
    fun getHideOverspendingWarn() = context.budgetDataStore.data.map {
        it[hideOverspendingWarnStoreKey] ?: false
    }


    suspend fun changeDisplayCurrency(currency: ExtendCurrency) {
        context.budgetDataStore.edit {
            it[currencyStoreKey] = currency.value ?: ""
        }
    }

    suspend fun changeRestedBudgetDistributionMethod(method: RestedBudgetDistributionMethod) {
        context.budgetDataStore.edit {
            it[restedBudgetDistributionMethodStoreKey] = method.toString()
        }
    }

    suspend fun hideOverspendingWarn(hide: Boolean) {
        context.budgetDataStore.edit {
            it[hideOverspendingWarnStoreKey] = hide
        }
    }

    suspend fun changeBudget(newBudget: BigDecimal, newFinishDate: Date) {
        context.budgetDataStore.edit {
            it[budgetStoreKey] = newBudget.toString()
            it[spentStoreKey] = BigDecimal.ZERO.toString()
            it[dailyBudgetStoreKey] = BigDecimal.ZERO.toString()
            it[spentFromDailyBudgetStoreKey] = BigDecimal.ZERO.toString()
            it[lastChangeDailyBudgetDateStoreKey] = roundToDay(Date()).time
            it[startPeriodDateStoreKey] = roundToDay(Date()).time
            it[finishPeriodDateStoreKey] = Date(roundToDay(newFinishDate).time + DAY - 1000).time
        }

        setDailyBudget(whatBudgetForDay())

        hideOverspendingWarn(false)
        spentDao.deleteAll()
    }

    suspend fun setDailyBudget(newDailyBudget: BigDecimal) {
        context.budgetDataStore.edit {
            val spent: BigDecimal = it[spentStoreKey]?.toBigDecimal()!!
            val spentFromDailyBudget: BigDecimal = it[spentFromDailyBudgetStoreKey]?.toBigDecimal()!!

            it[dailyBudgetStoreKey] = newDailyBudget.toString()
            it[spentStoreKey] = (spent + spentFromDailyBudget).toString()
            it[lastChangeDailyBudgetDateStoreKey] = roundToDay(Date()).time
            it[spentFromDailyBudgetStoreKey] = BigDecimal.ZERO.toString()
        }
    }

    suspend fun whatBudgetForDay(
        excludeCurrentDay: Boolean = false,
        notCommittedSpent: BigDecimal = BigDecimal.ZERO
    ): BigDecimal {
        val budget = getBudget().first()
        val spent = getSpent().first()
        val dailyBudget = getDailyBudget().first()
        val spentFromDailyBudget = getSpentFromDailyBudget().first()
        val finishPeriodDate = getFinishPeriodDate().first() ?: throw Exception("Finish period date is null")


        val restDays = countDaysToToday(finishPeriodDate) - if (excludeCurrentDay) 1 else 0
        var restBudget = budget - spent

        if (!excludeCurrentDay) {
            restBudget -= notCommittedSpent
        }

        restBudget -= if (excludeCurrentDay) {
            dailyBudget
        } else {
            spentFromDailyBudget
        }

        return restBudget
            .divide(
                restDays.toBigDecimal().coerceAtLeast(BigDecimal(1)),
                0,
                RoundingMode.FLOOR
            )
    }

    suspend fun howMuchBudgetRest(): BigDecimal {
        val budget = getBudget().first()
        val spent = getSpent().first()
        val spentFromDailyBudget = getSpentFromDailyBudget().first()

        return budget - spent - spentFromDailyBudget
    }

    suspend fun howMuchNotSpent(): BigDecimal {
        val budget = getBudget().first()
        val spent = getSpent().first()
        val dailyBudget = getDailyBudget().first()
        val spentFromDailyBudget = getSpentFromDailyBudget().first()
        val finishPeriodDate = getFinishPeriodDate().first() ?: throw Exception("Finish period date is null")
        val lastChangeDailyBudgetDate = getLastChangeDailyBudgetDate().first() ?: getStartPeriodDate().first()


        val restDays = countDaysToToday(finishPeriodDate)
        val skippedDays = abs(countDaysToToday(lastChangeDailyBudgetDate))
        val restBudget = budget - spent - dailyBudget

        return restBudget
            .divide((restDays + skippedDays - 1).coerceAtLeast(1).toBigDecimal())
            .multiply((skippedDays - 1).coerceAtLeast(0).toBigDecimal())
            .plus(dailyBudget - spentFromDailyBudget)
    }

    suspend fun addSpent(newSpent: Spent) {
        this.spentDao.insert(newSpent)

        context.budgetDataStore.edit {
            if (isToday(newSpent.date)) {
                val spentFromDailyBudget = it[spentFromDailyBudgetStoreKey]?.toBigDecimal()!!
                it[spentFromDailyBudgetStoreKey] = (spentFromDailyBudget + newSpent.value).toString()
            } else {
                val finishPeriodDate = it[finishPeriodDateStoreKey]?.let { value -> Date(value) }!!
                val dailyBudget = it[dailyBudgetStoreKey]?.toBigDecimal()!!
                val spent = it[spentStoreKey]?.toBigDecimal()!!

                val spreadDeltaSpentPerRestDays = newSpent.value
                    .divide(countDays(finishPeriodDate, newSpent.date).toBigDecimal())

                it[dailyBudgetStoreKey] = (dailyBudget + spreadDeltaSpentPerRestDays).toString()
                it[spentStoreKey] = (spent + newSpent.value).toString()
            }
        }
    }

    suspend fun removeSpent(spentForRemove: Spent) {
        this.spentDao.deleteById(spentForRemove.uid)

        context.budgetDataStore.edit {
            if (isToday(spentForRemove.date)) {
                val spentFromDailyBudget = it[spentFromDailyBudgetStoreKey]?.toBigDecimal()!!

                it[spentFromDailyBudgetStoreKey] = (spentFromDailyBudget - spentForRemove.value).toString()
            } else {
                val finishPeriodDate = it[finishPeriodDateStoreKey]?.let { value -> Date(value) }!!
                val dailyBudget = it[dailyBudgetStoreKey]?.toBigDecimal()!!
                val spent = it[spentStoreKey]?.toBigDecimal()!!

                val restDays = countDays(finishPeriodDate, spentForRemove.date)
                val spreadDeltaSpentPerRestDays = spentForRemove.value / restDays.toBigDecimal()

                it[dailyBudgetStoreKey] = (dailyBudget + spreadDeltaSpentPerRestDays).toString()
                it[spentStoreKey] = (spent - spentForRemove.value).toString()
            }
        }
    }
}