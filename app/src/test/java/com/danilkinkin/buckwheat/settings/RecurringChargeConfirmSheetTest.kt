package com.danilkinkin.buckwheat.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.di.buildTestUiHarness
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RecurringChargeConfirmSheetTest {

    @get:Rule
    val compose = createComposeRule()

    private fun pendingCharges() = listOf(
        Transaction(
            type = TransactionType.SPENT,
            value = 12.toBigDecimal(),
            date = Date(),
            comment = "gym",
        ),
        Transaction(
            type = TransactionType.SPENT,
            value = 7.5.toBigDecimal(),
            date = Date(),
            comment = "phone",
        ),
    )

    @Test
    fun addButtonRecordsPendingChargesAndClearsQueue() {
        val harness = buildTestUiHarness()
        harness.spendsViewModel.pendingRecurringCharges.value = pendingCharges()

        compose.setContent {
            BuckwheatTheme {
                RecurringChargeConfirmSheet(spendsViewModel = harness.spendsViewModel)
            }
        }

        compose.onNodeWithText("Add recurring payments?").assertIsDisplayed()
        compose.onNodeWithText("gym").assertIsDisplayed()
        compose.onNodeWithText("phone").assertIsDisplayed()
        compose.onNodeWithText("Add").assertIsDisplayed()
        compose.onNodeWithText("Skip").assertIsDisplayed()

        compose.onNodeWithText("Add").performClick()

        compose.waitUntil(timeoutMillis = 5_000) {
            harness.spendsViewModel.pendingRecurringCharges.value?.isEmpty() == true
        }

        val comments = harness.transactionDao.spends.map { it.comment }
        assertTrue("gym" in comments)
        assertTrue("phone" in comments)
        assertTrue(harness.transactionDao.spends.all { it.type == TransactionType.SPENT })
    }

    @Test
    fun skipButtonRecordsNothingAndClearsQueue() {
        val harness = buildTestUiHarness()
        harness.spendsViewModel.pendingRecurringCharges.value = pendingCharges()

        compose.setContent {
            BuckwheatTheme {
                RecurringChargeConfirmSheet(spendsViewModel = harness.spendsViewModel)
            }
        }

        compose.onNodeWithText("Skip").performClick()

        compose.waitUntil(timeoutMillis = 5_000) {
            harness.spendsViewModel.pendingRecurringCharges.value.orEmpty().isEmpty()
        }

        assertTrue(harness.transactionDao.spends.isEmpty())
    }
}
