package com.danilkinkin.buckwheat.patterns

import com.danilkinkin.buckwheat.util.toDate
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Deterministic tests for the window helpers (availableMonths / windowStartDate / applyWindow).
class PatternsWindowTest {
    private fun date(year: Int, month: Int, day: Int): Date = LocalDate.of(year, month, day).toDate()

    private fun spend(year: Int, month: Int, day: Int, value: String): PatternSpend =
        PatternSpend(date = date(year, month, day), value = BigDecimal(value), category = "food")

    private fun period(
        year: Int,
        month: Int,
        day: Int,
        budget: String = "0",
        spent: String = "0",
    ): PatternPeriod {
        val start = date(year, month, day)
        val finish = date(year, month, 28)
        return PatternPeriod(
            start = start,
            finish = finish,
            budget = BigDecimal(budget),
            totalSpent = BigDecimal(spent),
            isImported = false,
        )
    }

    private fun dataset(
        today: LocalDate = LocalDate.of(2026, Month.AUGUST, 16),
        spends: List<PatternSpend> = emptyList(),
        periods: List<PatternPeriod> = emptyList(),
    ): PatternDataset = PatternDataset(
        spends = spends,
        periods = periods,
        currencyCode = "USD",
        today = today,
    )

    // --- availableMonths ------------------------------------------------------

    @Test
    fun availableMonthsIsOneWithoutSpends() {
        assertEquals(1, availableMonths(dataset()))
    }

    @Test
    fun availableMonthsIsOneForCurrentMonthOnly() {
        val d = dataset(spends = listOf(spend(2026, 8, 3, "10")))
        assertEquals(1, availableMonths(d))
    }

    @Test
    fun availableMonthsSpansOldestSpendToTodayInclusive() {
        val d = dataset(spends = listOf(spend(2026, 4, 3, "10"), spend(2026, 8, 3, "20")))
        assertEquals(5, availableMonths(d))
    }

    @Test
    fun availableMonthsIgnoresMonthsWithNoSpend() {
        val d = dataset(spends = listOf(spend(2026, 2, 3, "10"), spend(2026, 8, 3, "20")))
        assertEquals(7, availableMonths(d))
    }

    // --- windowStartDate ------------------------------------------------------

    @Test
    fun windowStartDateIsNullForAll() {
        assertNull(windowStartDate(dataset(), PatternWindow(months = 6, allData = true)))
    }

    @Test
    fun windowStartDateIsFirstOfStartMonth() {
        val start = windowStartDate(dataset(), PatternWindow(months = 3, allData = false))
        assertEquals(date(2026, 6, 1), start)
    }

    @Test
    fun windowStartDateCoercesZeroToCurrentMonth() {
        val start = windowStartDate(dataset(), PatternWindow(months = 0, allData = false))
        assertEquals(date(2026, 8, 1), start)
    }

    // --- applyWindow ----------------------------------------------------------

    @Test
    fun applyWindowAllKeepsEverything() {
        val d = dataset(
            spends = listOf(spend(2025, 1, 3, "10"), spend(2026, 8, 3, "20")),
            periods = listOf(period(2025, 1, 1), period(2026, 7, 1)),
        )
        assertEquals(d, applyWindow(d, PatternWindow(months = 6, allData = true)))
    }

    @Test
    fun applyWindowKeepsSpendsAtOrAfterTheStartMonth() {
        val d = dataset(spends = listOf(
            spend(2026, 3, 5, "10"),
            spend(2026, 6, 5, "20"),
            spend(2026, 8, 5, "30"),
        ))
        val windowed = applyWindow(d, PatternWindow(months = 3, allData = false))
        assertEquals(listOf("20", "30"), windowed.spends.map { it.value.toPlainString() })
    }

    @Test
    fun applyWindowDropsPeriodsFinishedBeforeTheWindow() {
        val d = dataset(
            spends = listOf(spend(2026, 8, 5, "30")),
            periods = listOf(
                period(2026, 2, 1, budget = "100", spent = "80"),
                period(2026, 6, 1, budget = "100", spent = "80"),
            ),
        )
        val windowed = applyWindow(d, PatternWindow(months = 3, allData = false))
        assertEquals(1, windowed.periods.size)
        assertEquals(date(2026, 6, 1), windowed.periods.single().start)
    }

    @Test
    fun applyWindowKeepsCurrentPeriodEvenWhenItStartedBeforeTheWindow() {
        val d = dataset(
            spends = listOf(spend(2026, 8, 5, "30")),
            periods = listOf(
                PatternPeriod(
                    start = date(2026, 4, 1),
                    finish = date(2026, 8, 31),
                    budget = BigDecimal("100"),
                    totalSpent = BigDecimal("80"),
                    isImported = false,
                ),
            ),
        )
        val windowed = applyWindow(d, PatternWindow(months = 1, allData = false))
        assertEquals(1, windowed.periods.size)
        assertTrue(windowed.periods.single().finish.after(date(2026, 8, 1)))
    }
}
