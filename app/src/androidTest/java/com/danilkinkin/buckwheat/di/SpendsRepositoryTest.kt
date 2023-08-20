package com.danilkinkin.buckwheat.di

import android.util.Log
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.danilkinkin.buckwheat.MainActivity
import com.danilkinkin.buckwheat.data.entities.Spent
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import com.danilkinkin.buckwheat.util.toLocalDateTime
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
            FakeSpendsDao(),
            currentDateUseCase,
        )
    }

    // Set budget 1000 for 10 days
    // Start daily budget 100
    private suspend fun setBudget() {
        spendsRepository.changeBudget(
            1000.toBigDecimal(),
            currentDateUseCase.value.toLocalDate().plusDays(9).toDate()
        )
    }

    // Update daily budget. Should be called after change day
    private suspend fun distributeBudget() {
        spendsRepository.setDailyBudget(spendsRepository.whatBudgetForDay(
            applyTodaySpends = true,
        ))
    }

    private fun rewindTime(days: Long, hours: Long = 0) {
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

        assert(spendsRepository.getBudget().first() == 1000.toBigDecimal())
        assert(spendsRepository.getDailyBudget().first() == 100.toBigDecimal())
    }

    // Check budget set correctly distribute after change day
    @Test
    fun reCalcBudgetAfterChangeDayTest() = runTest {
        setBudget()
        rewindTime(1, 2)

        assert(spendsRepository.howMuchNotSpent() == 100.toBigDecimal().setScale(2))

        distributeBudget()

        assert(spendsRepository.getBudget().first() == 1000.toBigDecimal())
        assert(spendsRepository.getDailyBudget().first() == 111.toBigDecimal())
    }

    // Check budget set correctly distribute after change few days
    @Test
    fun reCalcBudgetAfterSkipFewDayTest() = runTest {
        setBudget()
        rewindTime(2, 4)

        assert(spendsRepository.howMuchNotSpent() == 200.toBigDecimal().setScale(2))

        distributeBudget()

        assert(spendsRepository.getBudget().first() == 1000.toBigDecimal())
        assert(spendsRepository.getDailyBudget().first() == 125.toBigDecimal())
    }

    // Check spent in same day added correctly
    @Test
    fun addSpentTest() = runTest {
        setBudget()

        val spend = Spent(10.toBigDecimal(), currentDateUseCase.value)
        spendsRepository.addSpent(spend)

        assert(spendsRepository.getAllSpends().value!!.contains(spend))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 10.toBigDecimal())
    }

    // Check spent in previous day added correctly
    @Test
    fun addSpentInPreviousDayTest() = runTest {
        setBudget()

        val spend = Spent(10.toBigDecimal(), currentDateUseCase.value)

        Log.d("SpendsRepositoryTest", "whatBudgetForDay: ${spendsRepository.whatBudgetForDay()}")

        rewindTime(1, 2)

        Log.d("SpendsRepositoryTest", "whatBudgetForDay: ${spendsRepository.whatBudgetForDay()}")

        distributeBudget()

        spendsRepository.addSpent(spend)

        Log.d("SpendsRepositoryTest", "spentFromDailyBudget: ${spendsRepository.getSpentFromDailyBudget().first()}")
        Log.d("SpendsRepositoryTest", "dailyBudget: ${spendsRepository.getDailyBudget().first()}")
        Log.d("SpendsRepositoryTest", "spent: ${spendsRepository.getSpent().first()}")

        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal())
        assert(spendsRepository.getDailyBudget().first() == 110.toBigDecimal())
        assert(spendsRepository.getSpent().first() == 10.toBigDecimal())
    }

    // Check today spent removed correctly
    @Test
    fun removeSpendTest() = runTest {
        setBudget()

        val spend_1 = Spent(
            value = 10.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        val spend_2 = Spent(
            value = 20.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend_1)
        spendsRepository.addSpent(spend_2)
        spendsRepository.removeSpent(spend_1)
        val spends = spendsRepository.getAllSpends().value!!

        assert(spends.isEmpty())
        assert(spendsRepository.getSpentFromDailyBudget().first() == 20.toBigDecimal())
    }

    // Add spent and remove in another day
    @Test
    fun removeSpendInAnotherDayTest() = runTest {
        setBudget()

        val spend = Spent(
            value = 10.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)

        rewindTime(1, 2)
        distributeBudget()

        spendsRepository.removeSpent(spend)
        val spends = spendsRepository.getAllSpends().value!!

        Log.d("SpendsRepositoryTest", "spentFromDailyBudget: ${spendsRepository.getSpentFromDailyBudget().first()}")
        Log.d("SpendsRepositoryTest", "dailyBudget: ${spendsRepository.getDailyBudget().first()}")
        Log.d("SpendsRepositoryTest", "spent: ${spendsRepository.getSpent().first()}")

        assert(spends.isEmpty())
        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal())
        assert(spendsRepository.getDailyBudget().first() == 111.toBigDecimal())
        assert(spendsRepository.getSpent().first() == 0.toBigDecimal())
    }

    // Cancel remove spent
    @Test
    fun removeAndReturnSpentTest() = runTest {
        setBudget()

        val spend = Spent(
            value = 10.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)
        spendsRepository.removeSpent(spend)
        spendsRepository.addSpent(spend)
        val spends = spendsRepository.getAllSpends().value!!

        assert(spends.contains(spend))
        assert(spends.size == 1)
        assert(spendsRepository.getSpentFromDailyBudget().first() == 10.toBigDecimal())
    }

    // Cancel remove spent in another day
    @Test
    fun removeAndReturnSpentInAnotherDayTest() = runTest {
        setBudget()

        val spend = Spent(
            value = 10.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)

        rewindTime(1, 2)
        distributeBudget()

        spendsRepository.removeSpent(spend)
        spendsRepository.addSpent(spend)
        val spends = spendsRepository.getAllSpends().value!!

        assert(spends.contains(spend))
        assert(spends.size == 1)
        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal())
        assert(spendsRepository.getDailyBudget().first() == 110.toBigDecimal())
    }

    // Change day of spent
    @Test
    fun changeDayOfSpentTest() = runTest {
        setBudget()

        val spend = Spent(
            value = 10.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)

        rewindTime(2, 4)

        distributeBudget()

        spendsRepository.removeSpent(spend)

        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal())

        spendsRepository.addSpent(spend.copy(date = currentDateUseCase.value))

        assert(spendsRepository.getSpentFromDailyBudget().first() == 10.toBigDecimal())
    }

    // Check overdraft
    @Test
    fun overdraft() = runTest {
        setBudget()

        val spend = Spent(
            value = 120.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)


        assert(spendsRepository.getSpentFromDailyBudget().first() == 120.toBigDecimal())

        Log.d("SpendsRepositoryTest", "whatBudgetForDay: ${spendsRepository.whatBudgetForDay(excludeCurrentDay = true, applyTodaySpends = true)}")
        // (1000 - 120) / 9 = 97.78
        assert(spendsRepository.whatBudgetForDay(excludeCurrentDay = true, applyTodaySpends = true) == 97.toBigDecimal())

        rewindTime(1, 2)
        distributeBudget()

        Log.d("SpendsRepositoryTest", "whatBudgetForDay: ${spendsRepository.whatBudgetForDay()}")
        // (1000 - 120) / 9 = 97.78
        assert(spendsRepository.whatBudgetForDay() == 97.toBigDecimal())
    }
}