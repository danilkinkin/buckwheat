package com.danilkinkin.buckwheat.di

import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.danilkinkin.buckwheat.MainActivity
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import com.danilkinkin.buckwheat.util.toLocalDateTime
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class SpendsRepositoryTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var spendsRepository: SpendsRepository

    val currentDateUseCase: FakeGetCurrentDateUseCase = FakeGetCurrentDateUseCase()

    @Before
    fun init() {
        spendsRepository = SpendsRepository(
            context = composeTestRule.activity,
            FakeTransactionDao(),
            currentDateUseCase,
        )
    }

    // Set budget 1000 for 10 days
    // Start daily budget 100
    private suspend fun setBudget(budget: Long = 1000, days: Long = 9) {
        spendsRepository.setBudget(
            budget.toBigDecimal(),
            currentDateUseCase.value.toLocalDate().plusDays(days).toDate()
        )
    }

    // Update daily budget. Should be called after change day
    private suspend fun distributeBudget() {
        spendsRepository.setDailyBudget(spendsRepository.whatBudgetForDay(
            applyTodaySpends = true,
        ))
    }

    private suspend fun distributeBudgetAddToday() {
        val notSpent = spendsRepository.howMuchNotSpent(
            excludeSkippedPart = true,
        )
        val dailyBudget = spendsRepository.nextDayBudget()
        val whatBudgetForDay = spendsRepository.whatBudgetForDay(
            excludeCurrentDay = false,
            applyTodaySpends = true,
            notCommittedSpent =  dailyBudget,
        )

        Log.d("SpendsRepositoryTest", "notSpent = $notSpent dailyBudget = $dailyBudget whatBudgetForDay = $whatBudgetForDay")

        spendsRepository.setDailyBudget(notSpent)
    }

    private fun rewindTime(days: Long, hours: Long = 0) {
        Log.d("SpendsRepositoryTest", "rewind time for days = $days hours = $hours")
        currentDateUseCase.value = currentDateUseCase.value
            .toLocalDateTime()
            .plusDays(days)
            .plusHours(hours)
            .toDate()
    }

    // Check budget set correctly
    @Test
    fun setBudgetTest() = runTest {
        setBudget()

        assertEquals(1000.toBigDecimal().setScale(2), spendsRepository.getBudget().first())
        assertEquals(100.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
    }

    // Check budget set correctly distribute after change day
    @Test
    fun reCalcBudgetAfterChangeDayTest() = runTest {
        setBudget()
        rewindTime(1)

        assertEquals(200.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        distributeBudget()

        assertEquals(1000.toBigDecimal().setScale(2), spendsRepository.getBudget().first())
        assertEquals(111.11.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
    }

    // Check budget set correctly distribute after change few days
    @Test
    fun reCalcBudgetAfterSkipFewDayTest() = runTest {
        setBudget()
        rewindTime(2)

        assertEquals(300.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        distributeBudget()

        assertEquals(1000.toBigDecimal().setScale(2), spendsRepository.getBudget().first())
        assertEquals(125.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
    }

    // Check budget set correctly distribute after change few days
    // Init budget 1000 for 10 days
    // [Day 1] Spend 10 | dailyBudget = 1000 / 10 = 100 | not spent = 90
    // [Day 2] Skip | dailyBudget = (1000 - 10) / 9 = 110 | not spent = 190
    // [Day 3] Skip | dailyBudget = (1000 - 10) / 8 = 125 | not spent = 290
    // [Day 4] Skip | dailyBudget = (1000 - 10) / 7 = 142.86 | not spent = 390
    // [Day 5] Skip | dailyBudget = (1000 - 10) / 6 = 166.67 | not spent = 490
    // [Day 6] Skip | dailyBudget = (1000 - 10) / 5 = 200 | not spent = 590
    // [Day 7] Skip | dailyBudget = (1000 - 10) / 4 = 250 | not spent = 690
    // [Day 8] Skip | dailyBudget = (1000 - 10) / 3 = 333.33 | not spent = 790
    // [Day 9] Skip | dailyBudget = (1000 - 10) / 2 = 500 | not spent = 890
    // [Day 10] Skip | dailyBudget = (1000 - 10) / 1 = 990 | not spent = 990
    @Test
    fun reCalcBudgetAfterSkipFewDayWithSpentTest() = runTest {
        setBudget()

        spendsRepository.addSpent(Transaction(TransactionType.SPENT, 10.toBigDecimal(), currentDateUseCase.value))

        assertEquals(90.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())
        rewindTime(1)
        assertEquals(190.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())
        rewindTime(1)
        assertEquals(290.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())
        rewindTime(1)
        assertEquals(390.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())
        rewindTime(1)
        assertEquals(490.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())
        rewindTime(1)
        assertEquals(590.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())
        rewindTime(1)
        assertEquals(690.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())
        rewindTime(1)
        assertEquals(790.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())
        rewindTime(1)
        assertEquals(890.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())
        rewindTime(1)
        assertEquals(990.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        distributeBudget()

        assertEquals(1000.toBigDecimal().setScale(2), spendsRepository.getBudget().first())
        assertEquals(990.toBigDecimal().setScale(2), spendsRepository.getDailyBudget().first())
    }

    // Check spent in same day added correctly
    @Test
    fun addSpentTest() = runTest {
        setBudget()

        val spend = Transaction(TransactionType.SPENT, 10.toBigDecimal(), currentDateUseCase.value)
        spendsRepository.addSpent(spend)

        assertTrue(spendsRepository.getAllSpends().value!!.contains(spend))
        assertEquals(10.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
    }

    // Check spent in previous day added correctly
    // Init budget 1000 for 10 days
    // [Day 1] dailyBudget = 1000 / 10 = 100 > No spent > not spent = 100
    // [Day 2] dailyBudget = 1000 / 9 = 111.11 > Spend 10 (to yesterday) > 111.11 - (10 / 9) = 110
    @Test
    fun addSpentInPreviousDayTest() = runTest {
        setBudget()

        val spend = Transaction(TransactionType.SPENT, 10.toBigDecimal(), currentDateUseCase.value)

        Log.d("SpendsRepositoryTest", "whatBudgetForDay: ${spendsRepository.whatBudgetForDay()}")

        rewindTime(1)

        Log.d("SpendsRepositoryTest", "whatBudgetForDay: ${spendsRepository.whatBudgetForDay()}")

        distributeBudget()

        spendsRepository.addSpent(spend)

        Log.d("SpendsRepositoryTest", "whatBudgetForDay: ${spendsRepository.whatBudgetForDay()}")
        Log.d("SpendsRepositoryTest", "spentFromDailyBudget: ${spendsRepository.getSpentFromDailyBudget().first()}")
        Log.d("SpendsRepositoryTest", "dailyBudget: ${spendsRepository.nextDayBudget()}")
        Log.d("SpendsRepositoryTest", "spent: ${spendsRepository.getSpent().first()}")

        assertEquals(0.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals(110.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(10.toBigDecimal().setScale(2), spendsRepository.getSpent().first())
    }

    // Check today spent removed correctly
    @Test
    fun removeSpendTest() = runTest {
        setBudget()

        val spend_1 = Transaction(
            type = TransactionType.SPENT,
            value = 10.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        val spend_2 = Transaction(
            type = TransactionType.SPENT,
            value = 20.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend_1)
        spendsRepository.addSpent(spend_2)
        spendsRepository.removeSpent(spend_1)
        val spends = spendsRepository.getAllSpends().value!!

        assertTrue(spends.isEmpty())
        assertEquals(20.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
    }

    // Add spent and remove in another day
    @Test
    fun removeSpendInAnotherDayTest() = runTest {
        setBudget()

        val spend = Transaction(
            type = TransactionType.SPENT,
            value = 10.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)

        rewindTime(1)
        distributeBudget()

        spendsRepository.removeSpent(spend)
        val spends = spendsRepository.getAllSpends().value!!

        Log.d("SpendsRepositoryTest", "spentFromDailyBudget: ${spendsRepository.getSpentFromDailyBudget().first()}")
        Log.d("SpendsRepositoryTest", "dailyBudget: ${spendsRepository.nextDayBudget()}")
        Log.d("SpendsRepositoryTest", "spent: ${spendsRepository.getSpent().first()}")

        assertTrue(spends.isEmpty())
        assertEquals(0.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals(111.11.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(0.toBigDecimal().setScale(2), spendsRepository.getSpent().first())
    }

    // Cancel remove spent
    @Test
    fun removeAndReturnSpentTest() = runTest {
        setBudget()

        val spend = Transaction(
            type = TransactionType.SPENT,
            value = 10.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)
        spendsRepository.removeSpent(spend)
        spendsRepository.addSpent(spend)
        val spends = spendsRepository.getAllSpends().value!!

        assertTrue(spends.contains(spend))
        assertEquals(1, spends.size)
        assertEquals(10.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
    }

    // Cancel remove spent in another day
    // Init budget 1000 for 10 days
    // [Day 1] Spend 10 > dailyBudget = 1000 / 10 = 100 > not spent = 90
    // [Day 2] No spent > dailyBudget = (1000 - 10) / 9 = 110 > Remove Spend 10 > dailyBudget = 1000 / 9 = 111.11 > not spent = 101.11
    @Test
    fun removeAndReturnSpentInAnotherDayTest() = runTest {
        setBudget()

        val spend = Transaction(
            type = TransactionType.SPENT,
            value = 10.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)

        rewindTime(1)
        distributeBudget()

        spendsRepository.removeSpent(spend)
        spendsRepository.addSpent(spend)
        val spends = spendsRepository.getAllSpends().value!!

        assertTrue(spends.contains(spend))
        assertEquals(1, spends.size)
        assertEquals(0.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals(110.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
    }

    // Change day of spent
    @Test
    fun changeDayOfSpentTest() = runTest {
        setBudget()

        val spend = Transaction(
            type = TransactionType.SPENT,
            value = 10.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)

        rewindTime(2)

        distributeBudget()

        spendsRepository.removeSpent(spend)

        assertEquals(0.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())

        spendsRepository.addSpent(spend.copy(date = currentDateUseCase.value))

        assertEquals(10.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
    }

    // Check overdraft
    // Init budget 1000 for 10 days
    // [Day 1] Spend 120 | dailyBudget = 1000 / 10 = 100 | not spent = -20
    // [Day 2] Skip | dailyBudget = (1000 - 120) / 9 = 97.78 | not spent = 80
    // [Day 3] Skip | dailyBudget = (1000 - 120) / 8 = 98.75 | not spent = 180
    @Test
    fun overdraft() = runTest {
        setBudget()

        val spend = Transaction(
            type = TransactionType.SPENT,
            value = 120.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)


        assertEquals(120.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())

        Log.d("SpendsRepositoryTest", "whatBudgetForDay: ${spendsRepository.whatBudgetForDay(excludeCurrentDay = true, applyTodaySpends = true)}")
        // (1000 - 120) / 9 = 97.78
        assertEquals(97.78.toBigDecimal().setScale(2), spendsRepository.whatBudgetForDay(excludeCurrentDay = true, applyTodaySpends = true))

        rewindTime(1)
        distributeBudget()

        Log.d("SpendsRepositoryTest", "whatBudgetForDay: ${spendsRepository.whatBudgetForDay()}")
        // (1000 - 120) / 9 = 97.78
        assertEquals(97.78.toBigDecimal().setScale(2), spendsRepository.whatBudgetForDay())
    }

    // How much saved
    @Test
    fun saved() = runTest {
        setBudget()

        val spend = Transaction(
            type = TransactionType.SPENT,
            value = 10.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)

        rewindTime(1)

        assertEquals(90.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent() - spendsRepository.nextDayBudget())

        distributeBudget()
        rewindTime(1)

        assertEquals(110.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent() - spendsRepository.nextDayBudget())

        distributeBudget()
        rewindTime(2)

        assertEquals(247.5.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent() - spendsRepository.nextDayBudget())
    }

    // Add to today every day
    // Init budget 330 for 10 days
    // [Day 1] dailyBudget = 330 / 10 = 33 > No spent > not spent = 33
    // [Day 2] dailyBudget = (330 - 33) / 9 + 33 = 66 > No spent > not spent = 66
    // [Day 3] dailyBudget = (330 - 66) / 8 + 66 = 99 > No spent > not spent = 99
    @Test
    fun savedWithAdd() = runTest {
        setBudget(330)

        rewindTime(1)

        assertEquals(33.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent() - spendsRepository.nextDayBudget())

        distributeBudgetAddToday()
        rewindTime(1)

        assertEquals(66.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent() - spendsRepository.nextDayBudget())

        distributeBudgetAddToday()
        rewindTime(1)

        assertEquals(99.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent() - spendsRepository.nextDayBudget())
    }

    // Overdraft, add spent on next day and skip day
    // Init budget 1000 for 10 days
    // [Day 1] dailyBudget = 1000 / 10 = 100 > Spend 140 > not spent = -40
    // [Day 2] dailyBudget = (1000 - 140) / 9 = 95.56 > Spend 10 > not spent = 85.56
    // [Day 3] dailyBudget = (1000 - 150) / 8 = 106.25 > No spent > not spent = 106.25
    // [Day 4] dailyBudget = (1000 - 150) / 7 = 121.43 > No Spent > not spent = 121.43
    // [Day 5] dailyBudget = (1000 - 150) / 7 = 121.43 > Skip > not spent = 242.86
    // [Day 6] dailyBudget = (1000 - 150) / 7 = 121.43 > Skip > not spent = 364.29
    // [Day 7] dailyBudget = (1000 - 150) / 7 = 121.43 > Skip > not spent = 485.72
    // [Day 8] dailyBudget = (1000 - 150) / 3 = 283.33 > Spend 300 > not spent = -16.67
    // [Day 9] dailyBudget = (1000 - 450) / 2 = 275 > Spend 100 > not spent = 175
    // [Day 10] dailyBudget = (1000 - 550) / 1 = 450 > No Spent > not spent = 450
    // [Day 11] dailyBudget = (1000 - 550) / 1 = 450 > No Spent > not spent = 450
    // [Day 12] dailyBudget = (1000 - 550) / 1 = 450 > No Spent > not spent = 450
    @Test
    fun complexTest1() = runTest {
        setBudget()

        // [Day 1] dailyBudget = 1000 / 10 = 100 > Spend 140 > not spent = -40

        assertEquals(100.toBigDecimal().setScale(2), spendsRepository.whatBudgetForDay(applyTodaySpends = true))

        spendsRepository.addSpent(Transaction(TransactionType.SPENT, 140.toBigDecimal(), currentDateUseCase.value))

        assertEquals(100.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(140.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals((-40).toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 2] dailyBudget = (1000 - 140) / 9 = 95.56 > Spend 10 > not spent = 85.56

        distributeBudget()

        assertEquals(95.56.toBigDecimal().setScale(2), spendsRepository.whatBudgetForDay(applyTodaySpends = true))

        spendsRepository.addSpent(Transaction(TransactionType.SPENT, 10.toBigDecimal(), currentDateUseCase.value))

        assertEquals(95.56.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(10.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals(85.56.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 3] dailyBudget = (1000 - 150) / 8 = 106.25 > No spent > not spent = 106.25

        distributeBudget()

        assertEquals(106.25.toBigDecimal().setScale(2), spendsRepository.whatBudgetForDay(applyTodaySpends = true))
        assertEquals(106.25.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(0.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals(106.25.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 4] dailyBudget = (1000 - 150) / 7 = 121.43 > No Spent > not spent = 121.43

        distributeBudget()

        assertEquals(121.43.toBigDecimal().setScale(2), spendsRepository.whatBudgetForDay(applyTodaySpends = true))
        assertEquals(121.43.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(0.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals(121.43.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 5] dailyBudget = (1000 - 150) / 7 = 121.43 > Skip > not spent = 242.86

        assertEquals(141.67.toBigDecimal().setScale(2), spendsRepository.whatBudgetForDay(applyTodaySpends = true))
        assertEquals(121.43.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(0.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals(242.86.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 6] dailyBudget = (1000 - 150) / 7 = 121.43 > Skip > not spent = 364.29

        assertEquals(170.toBigDecimal().setScale(2), spendsRepository.whatBudgetForDay(applyTodaySpends = true))
        assertEquals(121.43.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(0.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals(364.29.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 7] dailyBudget = (1000 - 150) / 7 = 121.43 > Skip > not spent = 485.72

        assertEquals(212.5.toBigDecimal().setScale(2), spendsRepository.whatBudgetForDay(applyTodaySpends = true))
        assertEquals(121.43.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(0.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals(485.72.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 8] dailyBudget = (1000 - 150) / 3 = 283.33 > Spend 300 > not spent = -16.67

        distributeBudget()

        assertEquals(283.33.toBigDecimal().setScale(2), spendsRepository.whatBudgetForDay(applyTodaySpends = true))

        spendsRepository.addSpent(Transaction(TransactionType.SPENT, 300.toBigDecimal(), currentDateUseCase.value))

        assertEquals(283.34.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(300.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals((-16.67).toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 9] dailyBudget = (1000 - 450) / 2 = 275 > Spend 100 > not spent = 175

        distributeBudget()

        assertEquals(275.toBigDecimal().setScale(2), spendsRepository.whatBudgetForDay(applyTodaySpends = true))

        spendsRepository.addSpent(Transaction(TransactionType.SPENT, 100.toBigDecimal(), currentDateUseCase.value))

        assertEquals(275.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(100.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals(175.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 10] dailyBudget = (1000 - 550) / 1 = 450 > No Spent > not spent = 450

        assertEquals(450.toBigDecimal().setScale(2), spendsRepository.whatBudgetForDay(applyTodaySpends = true))
        assertEquals(275.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(100.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals(450.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 11] dailyBudget = (1000 - 550) / 1 = 450 > No Spent > not spent = 450

        assertEquals(450.toBigDecimal().setScale(2), spendsRepository.whatBudgetForDay(applyTodaySpends = true))
        assertEquals(450.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(100.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals(450.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 12] dailyBudget = (1000 - 550) / 1 = 450 > No Spent > not spent = 450

        assertEquals(450.toBigDecimal().setScale(2), spendsRepository.whatBudgetForDay(applyTodaySpends = true))
        assertEquals(450.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(100.toBigDecimal().setScale(2), spendsRepository.getSpentFromDailyBudget().first())
        assertEquals(450.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())
    }

    // Add to today every day
    // Init budget 330 for 10 days
    // [Day 1] dailyBudget = 330 / 10 = 33 > No spent > not spent = 33
    // [Day 2] dailyBudget = (330 - 33) / 9 + 33 = 66 > No spent > not spent = 66
    // [Day 3] dailyBudget = (330 - 66) / 8 + 66 = 99 > No spent > not spent = 99
    // [Day 4] dailyBudget = (330 - 99) / 7 + 99 = 132 > No spent > not spent = 132
    // [Day 5] dailyBudget = (330 - 132) / 6 + 132 = 165 > Skip > not spent = 165
    // [Day 6] dailyBudget = (330 - 165) / 5 + 165 = 198 > Skip > not spent = 198
    // [Day 7] dailyBudget = (330 - 198) / 4 + 198 = 231 > Skip > not spent = 231
    // [Day 8] dailyBudget = (330 - 231) / 3 + 231 = 264 > Spend 300 > not spent = 264
    // [Day 9] dailyBudget = (330 - 264) / 2 + 264 = 297 > Spend 100 > not spent = 297
    // [Day 10] dailyBudget = (330 - 297) / 1 + 297 = 330 > No Spent > not spent = 330
    @Test
    fun complexTest2() = runTest {
        setBudget(330)

        // [Day 1] dailyBudget = 330 / 10 = 33 > No spent > not spent = 33

        assertEquals(33.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(33.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 2] dailyBudget = (330 - 33) / 9 + 33 = 66 > No spent > not spent = 66

        distributeBudgetAddToday()

        assertEquals(33.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(66.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 3] dailyBudget = (330 - 66) / 8 + 66 = 99 > No spent > not spent = 99

        distributeBudgetAddToday()

        assertEquals(33.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(99.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 4] dailyBudget = (330 - 99) / 7 + 99 = 132 > No spent > not spent = 132

        distributeBudgetAddToday()

        assertEquals(33.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(132.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 5] dailyBudget = (330 - 132) / 6 + 132 = 165 > Skip > not spent = 165

        distributeBudgetAddToday()

        assertEquals(33.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(165.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 6] dailyBudget = (330 - 165) / 5 + 165 = 198 > Skip > not spent = 198

        distributeBudgetAddToday()

        assertEquals(33.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(198.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 7] dailyBudget = (330 - 198) / 4 + 198 = 231 > Skip > not spent = 231

        distributeBudgetAddToday()

        assertEquals(33.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(231.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 8] dailyBudget = (330 - 231) / 3 + 231 = 264 > Spend 300 > not spent = 264

        distributeBudgetAddToday()

        assertEquals(33.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(264.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 9] dailyBudget = (330 - 264) / 2 + 264 = 297 > Spend 100 > not spent = 297

        distributeBudgetAddToday()

        assertEquals(33.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(297.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())

        rewindTime(1)

        // [Day 10] dailyBudget = (330 - 297) / 1 + 297 = 330 > No Spent > not spent = 330

        distributeBudgetAddToday()

        assertEquals(0.toBigDecimal().setScale(2), spendsRepository.nextDayBudget())
        assertEquals(330.toBigDecimal().setScale(2), spendsRepository.howMuchNotSpent())
    }
}