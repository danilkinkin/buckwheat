package com.danilkinkin.buckwheat.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.categories.CategoryAssigner
import com.danilkinkin.buckwheat.data.categories.CategoryAssignmentScheduler
import com.danilkinkin.buckwheat.settings.RecurringPaymentsViewModel

// Shared fixtures for Robolectric Compose UI tests: real ViewModels wired to in-memory
// fake DAOs so interactions (confirm/skip, mode selection) exercise production logic.
data class TestUiHarness(
    val spendsViewModel: SpendsViewModel,
    val recurringPaymentsViewModel: RecurringPaymentsViewModel,
    val transactionDao: FakeTransactionDao,
    val settingsRepository: SettingsRepository,
)

fun buildTestUiHarness(): TestUiHarness {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val transactionDao = FakeTransactionDao()
    val budgetPeriodDao = FakeBudgetPeriodDao()
    val settingsRepository = SettingsRepository(context)
    val spendsRepository = SpendsRepository(
        context = context,
        transactionDao = transactionDao,
        savedTagDao = FakeSavedTagDao(),
        savedCategoryDao = FakeSavedCategoryDao(),
        budgetPeriodDao = budgetPeriodDao,
        getCurrentDateUseCase = FakeGetCurrentDateUseCase(),
        categoryAssignmentScheduler = CategoryAssignmentScheduler(
            CategoryAssigner(context, transactionDao, budgetPeriodDao)
        ),
    )
    return TestUiHarness(
        spendsViewModel = SpendsViewModel(
            context = context,
            savedStateHandle = SavedStateHandle(),
            spendsRepository = spendsRepository,
            recurringDao = FakeRecurringDao(),
            settingsRepository = settingsRepository,
        ),
        recurringPaymentsViewModel = RecurringPaymentsViewModel(
            recurringDao = FakeRecurringDao(),
            settingsRepository = settingsRepository,
        ),
        transactionDao = transactionDao,
        settingsRepository = settingsRepository,
    )
}
