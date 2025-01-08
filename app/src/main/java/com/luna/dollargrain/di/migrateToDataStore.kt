package com.luna.dollargrain.di

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import com.luna.dollargrain.budgetDataStore
import com.luna.dollargrain.data.RestedBudgetDistributionMethod
import com.luna.dollargrain.data.dao.StorageDao
import com.luna.dollargrain.settingsDataStore

//TODO: Remove after 01.01.2024. Need for migration to DataStore
suspend fun migrateToDataStore(
    context: Context,
    storage: StorageDao,
) {
    if (storage.get("budget") == null) {
        Log.d("MigrateToDataStore", "Old storage empty. Skip migration")
        return
    }

    Log.d("MigrateToDataStore", "Start migration to budgetDataStore...")

    context.budgetDataStore.edit {
        it[budgetStoreKey] = storage.get("budget")!!.value
        it[spentStoreKey] = storage.get("spent")!!.value
        it[dailyBudgetStoreKey] = storage.get("dailyBudget")!!.value
        it[spentFromDailyBudgetStoreKey] = storage.get("spentFromDailyBudget")!!.value
        it[lastChangeDailyBudgetDateStoreKey] = storage.get("lastReCalcBudgetDate")!!.value.toLong()
        it[startPeriodDateStoreKey] = storage.get("startDate")!!.value.toLong()
        it[finishPeriodDateStoreKey] = storage.get("finishDate")!!.value.toLong()


        it[hideOverspendingWarnStoreKey] = storage.get("overspendingWarnHidden")?.value.toBoolean()
        it[currencyStoreKey] = storage.get("currency")?.value
            ?: ""
        it[restedBudgetDistributionMethodStoreKey] = storage.get("restedBudgetDistributionMethod")?.value
            ?: RestedBudgetDistributionMethod.ASK.name
    }

    Log.d("MigrateToDataStore", "Migration to budgetDataStore finished")

    Log.d("MigrateToDataStore", "Start migration to settingsDataStore...")

    context.settingsDataStore.edit {
        it[debugStoreKey] = storage.get("isDebug")?.value.toBoolean()
        it[showSpentCardByDefaultStoreKey] = !storage.get("showRestBudgetCardByDefault")?.value.toBoolean()
        it[TUTORS.SWIPE_EDIT_SPENT.key] = if (storage.get("tutorialSwipe")?.value.toBoolean()) {
            TUTORIAL_STAGE.PASSED.name
        } else {
            TUTORIAL_STAGE.NONE.name
        }
    }

    Log.d("MigrateToDataStore", "Migration to settingsDataStore finished")

    storage.deleteAll()
    Log.d("MigrateToDataStore", "Old storage cleared")
}