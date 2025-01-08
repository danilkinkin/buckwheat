package com.luna.dollargrain.di

import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.luna.dollargrain.MainActivity
import com.luna.dollargrain.data.entities.Transaction
import com.luna.dollargrain.data.entities.TransactionType
import com.luna.dollargrain.util.toDate
import com.luna.dollargrain.util.toLocalDate
import com.luna.dollargrain.util.toLocalDateTime
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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

        assert(spendsRepository.getBudget().first() == 1000.toBigDecimal().setScale(2))
        assert(spendsRepository.nextDayBudget() == 100.toBigDecimal().setScale(2))
    }

    // Check budget set correctly distribute after change day
    @Test
    fun reCalcBudgetAfterChangeDayTest() = runTest {
        setBudget()
        rewindTime(1)

        assert(spendsRepository.howMuchNotSpent() == 200.toBigDecimal().setScale(2))

        distributeBudget()

        assert(spendsRepository.getBudget().first() == 1000.toBigDecimal().setScale(2))
        assert(spendsRepository.nextDayBudget() == 111.11.toBigDecimal().setScale(2))
    }

    // Check budget set correctly distribute after change few days
    @Test
    fun reCalcBudgetAfterSkipFewDayTest() = runTest {
        setBudget()
        rewindTime(2)

        assert(spendsRepository.howMuchNotSpent() == 300.toBigDecimal().setScale(2))

        distributeBudget()

        assert(spendsRepository.getBudget().first() == 1000.toBigDecimal().setScale(2))
        assert(spendsRepository.nextDayBudget() == 125.toBigDecimal().setScale(2))
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

        assert(spendsRepository.howMuchNotSpent() == 90.toBigDecimal().setScale(2))
        rewindTime(1)
        assert(spendsRepository.howMuchNotSpent() == 190.toBigDecimal().setScale(2))
        rewindTime(1)
        assert(spendsRepository.howMuchNotSpent() == 290.toBigDecimal().setScale(2))
        rewindTime(1)
        assert(spendsRepository.howMuchNotSpent() == 390.toBigDecimal().setScale(2))
        rewindTime(1)
        assert(spendsRepository.howMuchNotSpent() == 490.toBigDecimal().setScale(2))
        rewindTime(1)
        assert(spendsRepository.howMuchNotSpent() == 590.toBigDecimal().setScale(2))
        rewindTime(1)
        assert(spendsRepository.howMuchNotSpent() == 690.toBigDecimal().setScale(2))
        rewindTime(1)
        assert(spendsRepository.howMuchNotSpent() == 790.toBigDecimal().setScale(2))
        rewindTime(1)
        assert(spendsRepository.howMuchNotSpent() == 890.toBigDecimal().setScale(2))
        rewindTime(1)
        assert(spendsRepository.howMuchNotSpent() == 990.toBigDecimal().setScale(2))

        distributeBudget()

        assert(spendsRepository.getBudget().first() == 1000.toBigDecimal().setScale(2))
        assert(spendsRepository.getDailyBudget().first() == 990.toBigDecimal().setScale(2))
    }

    // Check spent in same day added correctly
    @Test
    fun addSpentTest() = runTest {
        setBudget()

        val spend = Transaction(TransactionType.SPENT, 10.toBigDecimal(), currentDateUseCase.value)
        spendsRepository.addSpent(spend)

        assert(spendsRepository.getAllSpends().value!!.contains(spend))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 10.toBigDecimal().setScale(2))
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

        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal().setScale(2))
        assert(spendsRepository.nextDayBudget() == 110.toBigDecimal().setScale(2))
        assert(spendsRepository.getSpent().first() == 10.toBigDecimal().setScale(2))
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

        assert(spends.isEmpty())
        assert(spendsRepository.getSpentFromDailyBudget().first() == 20.toBigDecimal().setScale(2))
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

        assert(spends.isEmpty())
        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal().setScale(2))
        assert(spendsRepository.nextDayBudget() == 111.11.toBigDecimal().setScale(2))
        assert(spendsRepository.getSpent().first() == 0.toBigDecimal().setScale(2))
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

        assert(spends.contains(spend))
        assert(spends.size == 1)
        assert(spendsRepository.getSpentFromDailyBudget().first() == 10.toBigDecimal().setScale(2))
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

        assert(spends.contains(spend))
        assert(spends.size == 1)
        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal().setScale(2))
        assert(spendsRepository.nextDayBudget() == 110.toBigDecimal().setScale(2))
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

        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal().setScale(2))

        spendsRepository.addSpent(spend.copy(date = currentDateUseCase.value))

        assert(spendsRepository.getSpentFromDailyBudget().first() == 10.toBigDecimal().setScale(2))
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


        assert(spendsRepository.getSpentFromDailyBudget().first() == 120.toBigDecimal().setScale(2))

        Log.d("SpendsRepositoryTest", "whatBudgetForDay: ${spendsRepository.whatBudgetForDay(excludeCurrentDay = true, applyTodaySpends = true)}")
        // (1000 - 120) / 9 = 97.78
        assert(spendsRepository.whatBudgetForDay(excludeCurrentDay = true, applyTodaySpends = true) == 97.78.toBigDecimal().setScale(2))

        rewindTime(1)
        distributeBudget()

        Log.d("SpendsRepositoryTest", "whatBudgetForDay: ${spendsRepository.whatBudgetForDay()}")
        // (1000 - 120) / 9 = 97.78
        assert(spendsRepository.whatBudgetForDay() == 97.78.toBigDecimal().setScale(2))
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

        assert(spendsRepository.howMuchNotSpent() - spendsRepository.nextDayBudget() == 90.toBigDecimal().setScale(2))

        distributeBudget()
        rewindTime(1)

        assert(spendsRepository.howMuchNotSpent() - spendsRepository.nextDayBudget() == 110.toBigDecimal().setScale(2))

        distributeBudget()
        rewindTime(2)

        assert(spendsRepository.howMuchNotSpent() - spendsRepository.nextDayBudget() == 247.5.toBigDecimal().setScale(2))
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

        assert(spendsRepository.howMuchNotSpent() - spendsRepository.nextDayBudget() == 33.toBigDecimal().setScale(2))

        distributeBudgetAddToday()
        rewindTime(1)

        assert(spendsRepository.howMuchNotSpent() - spendsRepository.nextDayBudget() == 66.toBigDecimal().setScale(2))

        distributeBudgetAddToday()
        rewindTime(1)

        assert(spendsRepository.howMuchNotSpent() - spendsRepository.nextDayBudget() == 99.toBigDecimal().setScale(2))
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

        assert(spendsRepository.whatBudgetForDay(applyTodaySpends = true) == 100.toBigDecimal().setScale(2))

        spendsRepository.addSpent(Transaction(TransactionType.SPENT, 140.toBigDecimal(), currentDateUseCase.value))

        assert(spendsRepository.nextDayBudget() == 100.toBigDecimal().setScale(2))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 140.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == (-40).toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 2] dailyBudget = (1000 - 140) / 9 = 95.56 > Spend 10 > not spent = 85.56

        distributeBudget()

        assert(spendsRepository.whatBudgetForDay(applyTodaySpends = true) == 95.56.toBigDecimal().setScale(2))

        spendsRepository.addSpent(Transaction(TransactionType.SPENT, 10.toBigDecimal(), currentDateUseCase.value))

        assert(spendsRepository.nextDayBudget() == 95.56.toBigDecimal().setScale(2))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 10.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 85.56.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 3] dailyBudget = (1000 - 150) / 8 = 106.25 > No spent > not spent = 106.25

        distributeBudget()

        assert(spendsRepository.whatBudgetForDay(applyTodaySpends = true) == 106.25.toBigDecimal().setScale(2))
        assert(spendsRepository.nextDayBudget() == 106.25.toBigDecimal().setScale(2))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 106.25.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 4] dailyBudget = (1000 - 150) / 7 = 121.43 > No Spent > not spent = 121.43

        distributeBudget()

        assert(spendsRepository.whatBudgetForDay(applyTodaySpends = true) == 121.43.toBigDecimal().setScale(2))
        assert(spendsRepository.nextDayBudget() == 121.43.toBigDecimal().setScale(2))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 121.43.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 5] dailyBudget = (1000 - 150) / 7 = 121.43 > Skip > not spent = 242.86

        assert(spendsRepository.whatBudgetForDay(applyTodaySpends = true) == 141.67.toBigDecimal().setScale(2))
        assert(spendsRepository.nextDayBudget() == 121.43.toBigDecimal().setScale(2))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 242.86.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 6] dailyBudget = (1000 - 150) / 7 = 121.43 > Skip > not spent = 364.29

        assert(spendsRepository.whatBudgetForDay(applyTodaySpends = true) == 170.toBigDecimal().setScale(2))
        assert(spendsRepository.nextDayBudget() == 121.43.toBigDecimal().setScale(2))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 364.29.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 7] dailyBudget = (1000 - 150) / 7 = 121.43 > Skip > not spent = 485.72

        assert(spendsRepository.whatBudgetForDay(applyTodaySpends = true) == 212.5.toBigDecimal().setScale(2))
        assert(spendsRepository.nextDayBudget() == 121.43.toBigDecimal().setScale(2))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 485.72.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 8] dailyBudget = (1000 - 150) / 3 = 283.33 > Spend 300 > not spent = -16.67

        distributeBudget()

        assert(spendsRepository.whatBudgetForDay(applyTodaySpends = true) == 283.33.toBigDecimal().setScale(2))

        spendsRepository.addSpent(Transaction(TransactionType.SPENT, 300.toBigDecimal(), currentDateUseCase.value))

        assert(spendsRepository.nextDayBudget() == 283.34.toBigDecimal().setScale(2))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 300.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == (-16.67).toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 9] dailyBudget = (1000 - 450) / 2 = 275 > Spend 100 > not spent = 175

        distributeBudget()

        assert(spendsRepository.whatBudgetForDay(applyTodaySpends = true) == 275.toBigDecimal().setScale(2))

        spendsRepository.addSpent(Transaction(TransactionType.SPENT, 100.toBigDecimal(), currentDateUseCase.value))

        assert(spendsRepository.nextDayBudget() == 275.toBigDecimal().setScale(2))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 100.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 175.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 10] dailyBudget = (1000 - 550) / 1 = 450 > No Spent > not spent = 450

        assert(spendsRepository.whatBudgetForDay(applyTodaySpends = true) == 450.toBigDecimal().setScale(2))
        assert(spendsRepository.nextDayBudget() == 275.toBigDecimal().setScale(2))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 100.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 450.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 11] dailyBudget = (1000 - 550) / 1 = 450 > No Spent > not spent = 450

        assert(spendsRepository.whatBudgetForDay(applyTodaySpends = true) == 450.toBigDecimal().setScale(2))
        assert(spendsRepository.nextDayBudget() == 450.toBigDecimal().setScale(2))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 100.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 450.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 12] dailyBudget = (1000 - 550) / 1 = 450 > No Spent > not spent = 450

        assert(spendsRepository.whatBudgetForDay(applyTodaySpends = true) == 450.toBigDecimal().setScale(2))
        assert(spendsRepository.nextDayBudget() == 450.toBigDecimal().setScale(2))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 100.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 450.toBigDecimal().setScale(2))
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

        assert(spendsRepository.nextDayBudget() == 33.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 33.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 2] dailyBudget = (330 - 33) / 9 + 33 = 66 > No spent > not spent = 66

        distributeBudgetAddToday()

        assert(spendsRepository.nextDayBudget() == 33.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 66.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 3] dailyBudget = (330 - 66) / 8 + 66 = 99 > No spent > not spent = 99

        distributeBudgetAddToday()

        assert(spendsRepository.nextDayBudget() == 33.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 99.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 4] dailyBudget = (330 - 99) / 7 + 99 = 132 > No spent > not spent = 132

        distributeBudgetAddToday()

        assert(spendsRepository.nextDayBudget() == 33.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 132.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 5] dailyBudget = (330 - 132) / 6 + 132 = 165 > Skip > not spent = 165

        distributeBudgetAddToday()

        assert(spendsRepository.nextDayBudget() == 33.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 165.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 6] dailyBudget = (330 - 165) / 5 + 165 = 198 > Skip > not spent = 198

        distributeBudgetAddToday()

        assert(spendsRepository.nextDayBudget() == 33.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 198.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 7] dailyBudget = (330 - 198) / 4 + 198 = 231 > Skip > not spent = 231

        distributeBudgetAddToday()

        assert(spendsRepository.nextDayBudget() == 33.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 231.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 8] dailyBudget = (330 - 231) / 3 + 231 = 264 > Spend 300 > not spent = 264

        distributeBudgetAddToday()

        assert(spendsRepository.nextDayBudget() == 33.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 264.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 9] dailyBudget = (330 - 264) / 2 + 264 = 297 > Spend 100 > not spent = 297

        distributeBudgetAddToday()

        assert(spendsRepository.nextDayBudget() == 33.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 297.toBigDecimal().setScale(2))

        rewindTime(1)

        // [Day 10] dailyBudget = (330 - 297) / 1 + 297 = 330 > No Spent > not spent = 330

        distributeBudgetAddToday()

        assert(spendsRepository.nextDayBudget() == 0.toBigDecimal().setScale(2))
        assert(spendsRepository.howMuchNotSpent() == 330.toBigDecimal().setScale(2))
    }
}