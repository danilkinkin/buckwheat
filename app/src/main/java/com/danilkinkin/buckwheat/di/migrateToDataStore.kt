package com.danilkinkin.buckwheat.di

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import com.danilkinkin.buckwheat.budgetDataStore
import com.danilkinkin.buckwheat.data.RestedBudgetDistributionMethod
import com.danilkinkin.buckwheat.data.dao.StorageDao
import com.danilkinkin.buckwheat.settingsDataStore

/**
 * One-time migration from the legacy Room `storage` table to DataStore.
 *
 * Still required: users upgrading from a pre-DataStore version skip every intermediate
 * release, so the legacy table can appear at any time, not just before some 2024 deadline.
 *
 * Every key is read defensively instead of being forced with `!!`. The legacy table is only
 * partially populated on some old installs, and a missing date used to throw from a
 * LaunchedEffect during start-up - before the user could reach any screen.
 */
suspend fun migrateToDataStore(
    context: Context,
    storage: StorageDao,
) {
    if (storage.get("budget") == null) {
        Log.d("MigrateToDataStore", "Old storage empty. Skip migration")
        return
    }

    val budget = storage.get("budget")?.value
    val spent = storage.get("spent")?.value
    val dailyBudget = storage.get("dailyBudget")?.value
    val spentFromDailyBudget = storage.get("spentFromDailyBudget")?.value
    val lastReCalcBudgetDate = storage.get("lastReCalcBudgetDate")?.value?.toLongOrNull()
    val startDate = storage.get("startDate")?.value?.toLongOrNull()
    val finishDate = storage.get("finishDate")?.value?.toLongOrNull()

    if (budget == null ||
        spent == null ||
        dailyBudget == null ||
        spentFromDailyBudget == null ||
        lastReCalcBudgetDate == null ||
        startDate == null ||
        finishDate == null
    ) {
        Log.w("MigrateToDataStore", "Old storage is incomplete. Skip migration")
        return
    }

    val hideOverspendingWarn = (storage.get("overspendingWarnHidden")?.value ?: "false").toBoolean()
    val currency = storage.get("currency")?.value ?: ""
    val restedBudgetDistributionMethod = storage.get("restedBudgetDistributionMethod")?.value
        ?: RestedBudgetDistributionMethod.ASK.name

    Log.d("MigrateToDataStore", "Start migration to budgetDataStore...")

    context.budgetDataStore.edit {
        it[budgetStoreKey] = budget
        it[spentStoreKey] = spent
        it[dailyBudgetStoreKey] = dailyBudget
        it[spentFromDailyBudgetStoreKey] = spentFromDailyBudget
        it[lastChangeDailyBudgetDateStoreKey] = lastReCalcBudgetDate
        it[startPeriodDateStoreKey] = startDate
        it[finishPeriodDateStoreKey] = finishDate
        it[hideOverspendingWarnStoreKey] = hideOverspendingWarn
        it[currencyStoreKey] = currency
        it[restedBudgetDistributionMethodStoreKey] = restedBudgetDistributionMethod
    }

    Log.d("MigrateToDataStore", "Migration to budgetDataStore finished")

    val isDebug = (storage.get("isDebug")?.value ?: "false").toBoolean()
    val showRestBudgetCardByDefault =
        (storage.get("showRestBudgetCardByDefault")?.value ?: "false").toBoolean()
    val tutorialSwipePassed = (storage.get("tutorialSwipe")?.value ?: "false").toBoolean()

    Log.d("MigrateToDataStore", "Start migration to settingsDataStore...")

    context.settingsDataStore.edit {
        it[debugStoreKey] = isDebug
        it[showSpentCardByDefaultStoreKey] = !showRestBudgetCardByDefault
        it[TUTORS.SWIPE_EDIT_SPENT.key] = if (tutorialSwipePassed) {
            TUTORIAL_STAGE.PASSED.name
        } else {
            TUTORIAL_STAGE.NONE.name
        }
    }

    Log.d("MigrateToDataStore", "Migration to settingsDataStore finished")

    storage.deleteAll()
    Log.d("MigrateToDataStore", "Old storage cleared")
}
