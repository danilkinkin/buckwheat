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
import java.util.Date
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

    @Test
    fun setBudget() = runTest {
        spendsRepository.changeBudget(
            1000.toBigDecimal(),
            currentDateUseCase.value.toLocalDate().plusDays(4).toDate()
        )

        assert(spendsRepository.getBudget().first() == 1000.toBigDecimal())
        assert(spendsRepository.getDailyBudget().first() == 200.toBigDecimal())
    }

    @Test
    fun addSpent() = runTest {
        spendsRepository.changeBudget(
            1000.toBigDecimal(),
            currentDateUseCase.value.toLocalDate().plusDays(4).toDate()
        )

        val spend = Spent(
            value = 100.toBigDecimal(),
            date = currentDateUseCase.value,
        )
        spendsRepository.addSpent(spend)
        val spends = spendsRepository.getAllSpends().value!!

        assert(spends.contains(spend))
        assert(spendsRepository.getSpentFromDailyBudget().first() == 100.toBigDecimal())
    }

    @Test
    fun removeSpend() = runTest {
        spendsRepository.changeBudget(
            1000.toBigDecimal(),
            currentDateUseCase.value.toLocalDate().plusDays(5).toDate()
        )

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
    fun reCalcBudgetAfterChangeDay() = runTest {
        spendsRepository.changeBudget(
            1000.toBigDecimal(),
            currentDateUseCase.value.toLocalDate().plusDays(4).toDate()
        )

        currentDateUseCase.value = currentDateUseCase.value.toLocalDateTime().plusDays(1).plusHours(2).toDate()

        assert(spendsRepository.howMuchNotSpent() == 200.toBigDecimal().setScale(2))

        spendsRepository.setDailyBudget(spendsRepository.whatBudgetForDay())

        assert(spendsRepository.getBudget().first() == 1000.toBigDecimal())
        assert(spendsRepository.getDailyBudget().first() == 250.toBigDecimal())
    }

    @Test
    fun reCalcBudgetAfterSkipFewDay() = runTest {
        spendsRepository.changeBudget(
            1000.toBigDecimal(),
            currentDateUseCase.value.toLocalDate().plusDays(4).toDate()
        )

        currentDateUseCase.value = currentDateUseCase.value.toLocalDateTime().plusDays(2).plusHours(4).toDate()

        Log.d("reCalcBudgetAfterSkipFewDay", "lastChangeDailyBudgetDate: ${spendsRepository.getLastChangeDailyBudgetDate().first()}")
        Log.d("reCalcBudgetAfterSkipFewDay", "currentDate: ${currentDateUseCase.value}")

        assert(spendsRepository.howMuchNotSpent() == 400.toBigDecimal().setScale(2))

        spendsRepository.setDailyBudget(spendsRepository.whatBudgetForDay())

        Log.d("reCalcBudgetAfterSkipFewDay", "whatBudgetForDay: ${spendsRepository.whatBudgetForDay()}")
        Log.d("reCalcBudgetAfterSkipFewDay", "budget: ${spendsRepository.getBudget().first()}")
        Log.d("reCalcBudgetAfterSkipFewDay", "dailyBudget: ${spendsRepository.getDailyBudget().first()}")

        assert(spendsRepository.getBudget().first() == 1000.toBigDecimal())
        assert(spendsRepository.getDailyBudget().first() == 333.toBigDecimal())
    }

    @Test
    fun removeAndReturnSpent() = runTest {
        spendsRepository.changeBudget(
            1000.toBigDecimal(),
            currentDateUseCase.value.toLocalDate().plusDays(4).toDate()
        )

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
}