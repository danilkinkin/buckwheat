package com.danilkinkin.buckwheat.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * These are the first JVM unit tests in the project. They cover the budget arithmetic that
 * used to live inside SpendsRepository, where it could only be exercised by an instrumented
 * test on an emulator (app/src/androidTest/.../SpendsRepositoryTest.kt).
 */
class BudgetCalculatorTest {

    private fun money(value: String): BigDecimal =
        BigDecimal(value).setScale(BudgetCalculator.SCALE, BudgetCalculator.ROUNDING)

    private fun assertMoney(expected: String, actual: BigDecimal) {
        assertEquals(
            "expected $expected but was $actual",
            money(expected),
            actual.setScale(BudgetCalculator.SCALE, BudgetCalculator.ROUNDING),
        )
    }

    @Test
    fun `splits the budget evenly over the remaining days`() {
        val dailyBudget = BudgetCalculator.budgetForDay(
            budget = money("1000"),
            spent = money("0"),
            dailyBudget = money("0"),
            spentFromDailyBudget = money("0"),
            restDays = 10,
        )

        assertMoney("100.00", dailyBudget)
    }

    /**
     * Regression test: the widget used to compute this with `BigDecimal.divide(other)`, which
     * throws ArithmeticException when the quotient does not terminate. 900 / 7 used to crash
     * the widget on the third day of a ten day budget.
     */
    @Test
    fun `rounds non terminating divisions instead of throwing`() {
        val dailyBudget = BudgetCalculator.budgetForDay(
            budget = money("900"),
            spent = money("0"),
            dailyBudget = money("0"),
            spentFromDailyBudget = money("0"),
            restDays = 7,
        )

        assertMoney("128.57", dailyBudget)
    }

    @Test
    fun `never throws for the whole range of remaining days`() {
        for (restDays in 0..40) {
            for (spent in listOf("0", "3.33", "140", "999.99")) {
                BudgetCalculator.budgetForDay(
                    budget = money("1000"),
                    spent = money(spent),
                    dailyBudget = money("13.37"),
                    spentFromDailyBudget = money("7.77"),
                    restDays = restDays,
                    excludeCurrentDay = restDays % 2 == 0,
                    applyTodaySpends = restDays % 3 == 0,
                )
            }
        }
    }

    @Test
    fun `subtracts what was already spent today`() {
        // Day 2 of a 1000 / 10 days budget after spending 140 on day 1:
        // (1000 - 140) / 9 = 95.56
        val dailyBudget = BudgetCalculator.budgetForDay(
            budget = money("1000"),
            spent = money("140"),
            dailyBudget = money("100"),
            spentFromDailyBudget = money("0"),
            restDays = 9,
            applyTodaySpends = true,
        )

        assertMoney("95.56", dailyBudget)
    }

    @Test
    fun `excludeCurrentDay gives today back to the split`() {
        // (1000 - 0 - 100) / (10 - 1) = 100
        val dailyBudget = BudgetCalculator.budgetForDay(
            budget = money("1000"),
            spent = money("0"),
            dailyBudget = money("100"),
            spentFromDailyBudget = money("0"),
            restDays = 10,
            excludeCurrentDay = true,
        )

        assertMoney("100.00", dailyBudget)
    }

    @Test
    fun `clamps the divisor so an empty period cannot divide by zero`() {
        val dailyBudget = BudgetCalculator.budgetForDay(
            budget = money("100"),
            spent = money("0"),
            dailyBudget = money("0"),
            spentFromDailyBudget = money("0"),
            restDays = 0,
        )

        assertMoney("100.00", dailyBudget)
    }

    @Test
    fun `rolls unspent budget into today`() {
        // Mirrors "add to today": 330 for 10 days, nothing spent on day 1.
        // (330 - 33 * 1) / 9 * 1 + 33 = 66
        val notSpent = BudgetCalculator.notSpent(
            budget = money("330"),
            spent = money("0"),
            dailyBudget = money("33"),
            spentFromDailyBudget = money("0"),
            restDays = 9,
            skippedDays = 1,
            excludeSkippedPart = true,
        )

        assertMoney("66.00", notSpent)
    }

    @Test
    fun `next daily budget excludes the part already spent today`() {
        // (330 - 33) / 9 = 33, without adding today's budget back.
        val nextDailyBudget = BudgetCalculator.nextDailyBudget(
            budget = money("330"),
            spent = money("0"),
            dailyBudget = money("33"),
            spentFromDailyBudget = money("0"),
            restDays = 9,
            skippedDays = 1,
            excludeSkippedPart = true,
        )

        assertMoney("33.00", nextDailyBudget)
    }

    @Test
    fun `finished period returns what is left`() {
        // No days left: 1000 - 100 - 50
        assertMoney(
            "850.00",
            BudgetCalculator.notSpent(
                budget = money("1000"),
                spent = money("100"),
                dailyBudget = money("40"),
                spentFromDailyBudget = money("50"),
                restDays = 0,
                skippedDays = 3,
            ),
        )

        assertMoney(
            "850.00",
            BudgetCalculator.nextDailyBudget(
                budget = money("1000"),
                spent = money("100"),
                dailyBudget = money("40"),
                spentFromDailyBudget = money("50"),
                restDays = 0,
                skippedDays = 3,
            ),
        )
    }

    /**
     * End-to-end walk of the "add to today" strategy for the whole period, asserting the same
     * numbers that SpendsRepositoryTest.complexTest2 documents for a 330 budget over 10 days.
     */
    @Test
    fun `add to today keeps growing over a ten day period`() {
        val expected = listOf("66.00", "99.00", "132.00", "165.00", "198.00", "231.00", "264.00", "297.00", "330.00")

        var dailyBudget = money("33") // day 1: 330 / 10
        var restDays = 9

        expected.forEach { expectedDailyBudget ->
            dailyBudget = BudgetCalculator.notSpent(
                budget = money("330"),
                spent = money("0"),
                dailyBudget = dailyBudget,
                spentFromDailyBudget = money("0"),
                restDays = restDays,
                skippedDays = 1,
                excludeSkippedPart = true,
            )

            assertMoney(expectedDailyBudget, dailyBudget)
            restDays -= 1
        }
    }

    @Test
    fun `rest budget subtracts everything that was already spent`() {
        assertMoney(
            "850.00",
            BudgetCalculator.restBudget(
                budget = money("1000"),
                spent = money("100"),
                spentFromDailyBudget = money("50"),
            ),
        )
    }

    @Test
    fun `money uses bankers rounding`() {
        // 111.115 -> the tie goes to the even digit, not away from zero
        assertMoney("111.12", BudgetCalculator.money(BigDecimal("111.115")))
        assertMoney("111.14", BudgetCalculator.money(BigDecimal("111.145")))
    }
}
