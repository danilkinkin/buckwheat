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

    private suspend fun setBudget() {
        spendsRepository.changeBudget(
            1000.toBigDecimal(),
            currentDateUseCase.value.toLocalDate().plusDays(4).toDate()
        )
    }

    private fun rewindTime(days: Long, hours: Long = 0) {
        currentDateUseCase.value = currentDateUseCase.value
            .toLocalDateTime()
            .plusDays(days)
            .plusHours(hours)
            .toDate()
    }

    @Test
    fun setBudgetTest() = runTest {
        setBudget()

        assert(spendsRepository.getBudget().first() == 1000.toBigDecimal())
        assert(spendsRepository.getDailyBudget().first() == 200.toBigDecimal())
    }

    @Test
    fun addSpentTest() = runTest {
        setBudget()

        val spend = Spent(100.toBigDecimal(), currentDateUseCase.value)
        spendsRepository.addSpent(spend)
        val spends = spendsRepository.getAllSpends().value!!

        assert(spends.contains(spend))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 100.toBigDecimal())
    }

    @Test
    fun addSpentInPreviousDayTest() = runTest {
        setBudget()

        val spend = Spent(100.toBigDecimal(), currentDateUseCase.value)

        Log.d("SpendsRepositoryTest", "whatBudgetForDay: ${spendsRepository.whatBudgetForDay()}")

        rewindTime(1, 2)

        Log.d("SpendsRepositoryTest", "whatBudgetForDay: ${spendsRepository.whatBudgetForDay()}")

        spendsRepository.setDailyBudget(spendsRepository.whatBudgetForDay())

        spendsRepository.addSpent(spend)

        Log.d("SpendsRepositoryTest", "spentFromDailyBudget: ${spendsRepository.getSpentFromDailyBudget().first()}")
        Log.d("SpendsRepositoryTest", "dailyBudget: ${spendsRepository.getDailyBudget().first()}")
        Log.d("SpendsRepositoryTest", "spent: ${spendsRepository.getSpent().first()}")

        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal())
        assert(spendsRepository.getDailyBudget().first() == 225.toBigDecimal())
        assert(spendsRepository.getSpent().first() == 100.toBigDecimal())
    }

    @Test
    fun removeSpendTest() = runTest {
        setBudget()

        val spend = Spent(
            value = 200.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)
        spendsRepository.removeSpent(spend)
        val spends = spendsRepository.getAllSpends().value!!

        assert(spends.isEmpty())
    }

    @Test
    fun removeSpendInAnotherDayTest() = runTest {
        setBudget()

        val spend = Spent(
            value = 100.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)

        rewindTime(1, 2)
        spendsRepository.setDailyBudget(spendsRepository.whatBudgetForDay())

        spendsRepository.removeSpent(spend)
        val spends = spendsRepository.getAllSpends().value!!

        Log.d("SpendsRepositoryTest", "spentFromDailyBudget: ${spendsRepository.getSpentFromDailyBudget().first()}")
        Log.d("SpendsRepositoryTest", "dailyBudget: ${spendsRepository.getDailyBudget().first()}")
        Log.d("SpendsRepositoryTest", "spent: ${spendsRepository.getSpent().first()}")

        assert(spends.isEmpty())
        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal())
        assert(spendsRepository.getDailyBudget().first() == 250.toBigDecimal())
        assert(spendsRepository.getSpent().first() == 0.toBigDecimal())
    }

    @Test
    fun reCalcBudgetAfterChangeDayTest() = runTest {
        setBudget()
        rewindTime(1, 2)

        assert(spendsRepository.howMuchNotSpent() == 200.toBigDecimal().setScale(2))

        spendsRepository.setDailyBudget(spendsRepository.whatBudgetForDay())

        assert(spendsRepository.getBudget().first() == 1000.toBigDecimal())
        assert(spendsRepository.getDailyBudget().first() == 250.toBigDecimal())
    }

    @Test
    fun reCalcBudgetAfterSkipFewDayTest() = runTest {
        setBudget()
        rewindTime(2, 4)

        assert(spendsRepository.howMuchNotSpent() == 400.toBigDecimal().setScale(2))

        spendsRepository.setDailyBudget(spendsRepository.whatBudgetForDay())

        assert(spendsRepository.getBudget().first() == 1000.toBigDecimal())
        assert(spendsRepository.getDailyBudget().first() == 333.toBigDecimal())
    }

    @Test
    fun removeAndReturnSpentTest() = runTest {
        setBudget()

        val spend = Spent(
            value = 100.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)
        spendsRepository.removeSpent(spend)
        spendsRepository.addSpent(spend)
        val spends = spendsRepository.getAllSpends().value!!

        assert(spends.contains(spend))
        assert(spends.size == 1)
        assert(spendsRepository.getSpentFromDailyBudget().first() == 100.toBigDecimal())
    }

    @Test
    fun changeDayOfSpentTest() = runTest {
        setBudget()

        val spend = Spent(
            value = 100.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)

        rewindTime(2, 4)
        spendsRepository.setDailyBudget(spendsRepository.whatBudgetForDay())

        spendsRepository.removeSpent(spend)

        assert(spendsRepository.getSpentFromDailyBudget().first() == 0.toBigDecimal())

        spendsRepository.addSpent(spend.copy(date = currentDateUseCase.value))

        assert(spendsRepository.getSpentFromDailyBudget().first() == 100.toBigDecimal())
    }
}