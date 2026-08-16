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

// Deterministic pure-engine tests: every fixture is a fixed LocalDate, no Date() in expectations.
class PatternEngineTest {
    private fun date(year: Int, month: Int, day: Int): Date = LocalDate.of(year, month, day).toDate()

    private fun spend(year: Int, month: Int, day: Int, value: String, category: String? = null): PatternSpend =
        PatternSpend(date = date(year, month, day), value = BigDecimal(value), category = category)

    private fun period(
        year: Int,
        month: Int,
        day: Int,
        budget: String,
        spent: String,
        isImported: Boolean = false,
    ): PatternPeriod {
        val start = date(year, month, day)
        val finish = date(year, month, 28)
        return PatternPeriod(
            start = start,
            finish = finish,
            budget = BigDecimal(budget),
            totalSpent = BigDecimal(spent),
            isImported = isImported,
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

    private fun assertAmount(expected: String, actual: BigDecimal) {
        assertEquals("expected $expected but was $actual", 0, actual.compareTo(BigDecimal(expected)))
    }

    // --- normalizeName -------------------------------------------------------

    @Test
    fun normalizeNameTrimsAndCollapsesWhitespace() {
        assertEquals("food market", normalizeName("  Food   Market  "))
        assertEquals("lunch", normalizeName("lunch "))
    }

    @Test
    fun normalizeNameLowercasesInvariant() {
        assertEquals("food", normalizeName("FOOD"))
        assertEquals("netflix", normalizeName("NeTfLiX"))
    }

    @Test
    fun normalizeNameStripsTrailingPunctuation() {
        assertEquals("food", normalizeName("food!!"))
        assertEquals("netflix", normalizeName("netflix."))
        assertEquals("lunch", normalizeName("lunch -"))
    }

    @Test
    fun normalizeNameAppliesNfkc() {
        assertEquals("food", normalizeName("\uFF26\uFF2F\uFF2F\uFF24"))
        assertEquals("caf\u00e9", normalizeName("caf\u00e9"))
    }

    @Test
    fun normalizeNameBlankInput() {
        assertEquals("", normalizeName("   "))
        assertEquals("", normalizeName(""))
    }

    // --- levenshteinDistance ------------------------------------------------

    @Test
    fun levenshteinDistanceBasics() {
        assertEquals(1, levenshteinDistance("food", "foood"))
        assertEquals(1, levenshteinDistance("food", "foods"))
        assertEquals(2, levenshteinDistance("food", "fooods"))
        assertEquals(6, levenshteinDistance("food", "food court"))
        assertEquals(0, levenshteinDistance("food", "food"))
    }

    @Test
    fun levenshteinDistanceEdges() {
        assertEquals(0, levenshteinDistance("", ""))
        assertEquals(4, levenshteinDistance("", "food"))
        assertEquals(4, levenshteinDistance("food", ""))
    }

    // --- mergeCategoryVariants / canonicalCategory ---------------------------

    @Test
    fun mergeMergesTypoVariantsAndKeepsMajoritySpelling() {
        val variants = mergeCategoryVariants(listOf("Food", "Food", "food ", "foood", "foods"))
        assertEquals(mapOf("food" to "Food"), variants)
    }

    @Test
    fun mergeDoesNotCollapseDistinctCategories() {
        val variants = mergeCategoryVariants(listOf("food", "food court", "travel"))
        assertEquals(setOf("food", "food court", "travel"), variants.keys)
    }

    @Test
    fun mergeDisplayNamePicksMostUsedSpelling() {
        val variants = mergeCategoryVariants(listOf("food", "Food", "Food"))
        assertEquals("Food", variants.getValue("food"))
    }

    @Test
    fun canonicalCategoryResolvesVariantAndBlank() {
        val index = categoryNameIndex(listOf("Food", "food!!", "Travel"))
        assertEquals("food", canonicalCategory("FOOD ", index))
        assertEquals("food", canonicalCategory("food!!", index))
        assertEquals("travel", canonicalCategory("Travel", index))
        assertEquals(UNCATEGORIZED_KEY, canonicalCategory("", index))
        assertEquals(UNCATEGORIZED_KEY, canonicalCategory(null, index))
    }

    @Test
    fun emptyNamesProduceEmptyIndex() {
        val index = categoryNameIndex(emptyList())
        assertTrue(index.canonicalToDisplay.isEmpty())
        assertTrue(index.aliasToCanonical.isEmpty())
        assertEquals(UNCATEGORIZED_KEY, canonicalCategory(null, index))
    }

    // --- monthlyTotals ------------------------------------------------------

    @Test
    fun monthlyTotalsOldestFirstWithCurrentLast() {
        val points = monthlyTotals(
            dataset(
                spends = listOf(
                    spend(2026, 5, 12, "100", "Food"),
                    spend(2026, 6, 12, "150", "Food"),
                    spend(2026, 8, 2, "40", "Food"),
                ),
                periods = listOf(period(2026, 5, 1, "500", "100"), period(2026, 6, 1, "500", "150")),
            ),
        )
        assertEquals(listOf("May", "Jun", "Aug"), points.map { it.label })
        assertEquals(listOf(false, false, true), points.map { it.isCurrent })
        assertAmount("100", points[0].spent)
        assertAmount("500", points[0].budget!!)
        assertNull(points[2].budget)
    }

    @Test
    fun monthlyTotalsIncludeYearAcrossCalendarYears() {
        val points = monthlyTotals(
            dataset(
                spends = listOf(
                    spend(2025, 12, 20, "100", "Food"),
                    spend(2026, 1, 10, "100", "Food"),
                ),
            ),
        )
        assertEquals(listOf("Dec '25", "Jan '26"), points.map { it.label })
    }

    @Test
    fun monthlyTotalsEmptyDataset() {
        assertTrue(monthlyTotals(dataset()).isEmpty())
    }

    // --- trendDirection / trendPercent --------------------------------------

    private fun completedPoint(year: Int, month: Int, value: String): MonthlyPoint =
        MonthlyPoint(label = "m$month", spent = BigDecimal(value), budget = null, isCurrent = false)

    @Test
    fun trendDirectionUpForRisingCompletedMonths() {
        val points = listOf(
            completedPoint(2026, 5, "100"),
            completedPoint(2026, 6, "150"),
            completedPoint(2026, 7, "200"),
        )
        assertEquals(TrendDirection.UP, trendDirection(points))
    }

    @Test
    fun trendDirectionDownForFallingCompletedMonths() {
        val points = listOf(
            completedPoint(2026, 5, "200"),
            completedPoint(2026, 6, "150"),
            completedPoint(2026, 7, "100"),
        )
        assertEquals(TrendDirection.DOWN, trendDirection(points))
    }

    @Test
    fun trendDirectionStableWithinFivePercent() {
        val points = listOf(
            completedPoint(2026, 5, "100"),
            completedPoint(2026, 6, "102"),
            completedPoint(2026, 7, "101"),
        )
        assertEquals(TrendDirection.STABLE, trendDirection(points))
    }

    @Test
    fun trendDirectionIgnoresCurrentPartialMonth() {
        val points = listOf(
            completedPoint(2026, 5, "100"),
            completedPoint(2026, 6, "150"),
            completedPoint(2026, 7, "200"),
            MonthlyPoint(label = "Aug", spent = BigDecimal("900"), budget = null, isCurrent = true),
        )
        assertEquals(TrendDirection.UP, trendDirection(points))
    }

    @Test
    fun trendDirectionStableWithoutEnoughMonths() {
        assertEquals(TrendDirection.STABLE, trendDirection(listOf(completedPoint(2026, 5, "100"))))
        assertEquals(TrendDirection.STABLE, trendDirection(emptyList()))
    }

    @Test
    fun trendPercentRelativeChange() {
        assertEquals(25, trendPercent(BigDecimal("100"), BigDecimal("125")))
        assertEquals(-50, trendPercent(BigDecimal("100"), BigDecimal("50")))
        assertEquals(0, trendPercent(BigDecimal("0"), BigDecimal("100")))
    }

    // --- categoryPatterns ---------------------------------------------------

    @Test
    fun categoryPatternsMergeVariantSpellingsIntoOne() {
        val categories = categoryPatterns(
            dataset(
                spends = listOf(
                    spend(2026, 5, 1, "100", "Food"),
                    spend(2026, 5, 2, "50", "food!!"),
                    spend(2026, 5, 3, "30", "Travel"),
                ),
            ),
        )
        assertEquals(2, categories.size)
        val food = categories.first()
        assertEquals("food", food.key)
        assertEquals("Food", food.displayName)
        assertAmount("150", food.total)
        assertEquals(83, food.percent)
        assertEquals(1, food.activeMonths)
        assertAmount("150", food.monthlyAverage)
    }

    @Test
    fun categoryPatternsSortedByTotalDesc() {
        val categories = categoryPatterns(
            dataset(
                spends = listOf(
                    spend(2026, 5, 1, "10", "Food"),
                    spend(2026, 5, 2, "500", "Travel"),
                    spend(2026, 5, 3, "40", "Other"),
                ),
            ),
        )
        assertEquals(listOf("travel", "other", "food"), categories.map { it.key })
    }

    @Test
    fun categoryMonthlyAverageUsesActiveMonthsOnly() {
        val categories = categoryPatterns(
            dataset(
                spends = listOf(
                    spend(2026, 5, 1, "100", "Food"),
                    spend(2026, 6, 1, "100", "Food"),
                    spend(2026, 7, 1, "500", "Travel"),
                ),
            ),
        )
        val food = categories.first { it.key == "food" }
        assertEquals(3, food.monthCount)
        assertEquals(2, food.activeMonths)
        assertAmount("100", food.monthlyAverage)
    }

    @Test
    fun categoryTrendSplitFirstVsSecondHalf() {
        val rising = categoryPatterns(
            dataset(
                spends = listOf(
                    spend(2026, 5, 1, "100", "Food"),
                    spend(2026, 6, 1, "200", "Food"),
                ),
            ),
        ).first { it.key == "food" }
        assertEquals(TrendDirection.UP, rising.trend)

        val declining = categoryPatterns(
            dataset(
                spends = listOf(
                    spend(2026, 5, 1, "200", "Food"),
                    spend(2026, 6, 1, "100", "Food"),
                ),
            ),
        ).first { it.key == "food" }
        assertEquals(TrendDirection.DOWN, declining.trend)
    }

    @Test
    fun categoryPatternsEmptyDataset() {
        assertTrue(categoryPatterns(dataset()).isEmpty())
    }

    // --- concentrationIndex -------------------------------------------------

    @Test
    fun concentrationIndexConcentrated() {
        val index = concentrationIndex(
            dataset(
                spends = listOf(
                    spend(2026, 5, 1, "800", "Food"),
                    spend(2026, 5, 2, "100", "Travel"),
                    spend(2026, 5, 3, "100", "Other"),
                ),
            ),
        )
        assertEquals("Food", index.topCategory)
        assertEquals(80, index.topSharePercent)
        assertAmount("0.6600", index.herfindahl)
    }

    @Test
    fun concentrationIndexDiversified() {
        val index = concentrationIndex(
            dataset(
                spends = listOf(
                    spend(2026, 5, 1, "100", "Food"),
                    spend(2026, 5, 2, "100", "Travel"),
                    spend(2026, 5, 3, "100", "Other"),
                    spend(2026, 5, 4, "100", "Health"),
                ),
            ),
        )
        assertAmount("0.2500", index.herfindahl)
    }

    @Test
    fun concentrationIndexEmpty() {
        val index = concentrationIndex(dataset())
        assertNull(index.topCategory)
        assertEquals(0, index.topSharePercent)
        assertAmount("0", index.herfindahl)
    }

    // --- weekdayPatterns / weekendVsWeekdayDelta -----------------------------

    @Test
    fun weekdayPatternsZeroFillsAllDays() {
        val points = weekdayPatterns(
            dataset(
                spends = listOf(spend(2026, 8, 10, "100", "Food")),
            ),
        )
        assertEquals(7, points.size)
        val monday = points.first { it.day == java.time.DayOfWeek.MONDAY }
        assertEquals(1, monday.count)
        assertAmount("100", monday.total)
        assertEquals(100, monday.sharePercent)
        val sunday = points.first { it.day == java.time.DayOfWeek.SUNDAY }
        assertEquals(0, sunday.count)
        assertAmount("0", sunday.total)
    }

    @Test
    fun weekdaySharePercentMatchesTotals() {
        val points = weekdayPatterns(
            dataset(
                spends = listOf(
                    spend(2026, 8, 10, "100", "Food"),
                    spend(2026, 8, 11, "300", "Food"),
                ),
            ),
        )
        val monday = points.first { it.day == java.time.DayOfWeek.MONDAY }
        val tuesday = points.first { it.day == java.time.DayOfWeek.TUESDAY }
        assertEquals(25, monday.sharePercent)
        assertEquals(75, tuesday.sharePercent)
    }

    @Test
    fun weekendVsWeekdayDeltaSurfacesWeekendBlowout() {
        val delta = weekendVsWeekdayDelta(
            dataset(
                spends = listOf(
                    spend(2026, 8, 10, "100", "Food"),
                    spend(2026, 8, 11, "100", "Food"),
                    spend(2026, 8, 12, "100", "Food"),
                    spend(2026, 8, 13, "100", "Food"),
                    spend(2026, 8, 14, "100", "Food"),
                    spend(2026, 8, 15, "300", "Food"),
                    spend(2026, 8, 16, "300", "Food"),
                ),
            ),
        )
        assertEquals(200, delta)
    }

    @Test
    fun weekendVsWeekdayDeltaNullWithoutSpends() {
        assertNull(weekendVsWeekdayDelta(dataset()))
    }

    // --- dayOfMonthPattern / noSpendDays / busiestDay ------------------------

    @Test
    fun dayOfMonthPatternZeroFillsAllDays() {
        val points = dayOfMonthPattern(
            dataset(
                spends = listOf(
                    spend(2026, 8, 1, "50", "Food"),
                    spend(2026, 8, 15, "30", "Food"),
                ),
            ),
        )
        assertEquals(31, points.size)
        assertAmount("50", points[0].total)
        assertEquals(1, points[0].count)
        assertAmount("30", points[14].total)
        assertAmount("0", points[7].total)
    }

    @Test
    fun noSpendDaysCountsQuietDaysInRange() {
        val days = noSpendDays(
            dataset(
                spends = listOf(
                    spend(2026, 8, 1, "10", "Food"),
                    spend(2026, 8, 2, "10", "Food"),
                    spend(2026, 8, 3, "10", "Food"),
                    spend(2026, 8, 16, "10", "Food"),
                ),
            ),
        )
        // Range 1..16 = 16 days, 4 with spend -> 12 quiet.
        assertEquals(12, days)
    }

    @Test
    fun noSpendDaysEmpty() {
        assertEquals(0, noSpendDays(dataset()))
    }

    @Test
    fun busiestDayReturnsHighestSingleDay() {
        val busiest = busiestDay(
            dataset(
                spends = listOf(
                    spend(2026, 8, 1, "10", "Food"),
                    spend(2026, 8, 2, "500", "Food"),
                    spend(2026, 8, 2, "100", "Food"),
                    spend(2026, 8, 3, "10", "Food"),
                ),
            ),
        )
        assertEquals(LocalDate.of(2026, Month.AUGUST, 2), busiest!!.date)
        assertAmount("600", busiest.total)
    }

    @Test
    fun busiestDayEmpty() {
        assertNull(busiestDay(dataset()))
    }

    // --- budgetCompliance ---------------------------------------------------

    @Test
    fun budgetComplianceUtilizationAndOverspentFlag() {
        val compliance = budgetCompliance(
            dataset(
                periods = listOf(
                    period(2026, 5, 1, "1000", "1200"),
                    period(2026, 6, 1, "1000", "800"),
                ),
            ),
        )
        assertEquals(2, compliance.periods.size)
        assertEquals(120, compliance.periods[0].utilizationPercent)
        assertTrue(compliance.periods[0].isOverspent)
        assertEquals(80, compliance.periods[1].utilizationPercent)
        assertEquals(1, compliance.overspentCount)
    }

    @Test
    fun budgetComplianceExcludesCurrentPeriod() {
        val compliance = budgetCompliance(
            dataset(
                today = LocalDate.of(2026, Month.AUGUST, 16),
                periods = listOf(
                    period(2026, 5, 1, "1000", "1200"),
                    period(2026, 8, 1, "1000", "100"),
                ),
            ),
        )
        assertEquals(1, compliance.periods.size)
        assertEquals(LocalDate.of(2026, Month.MAY, 1), compliance.periods[0].start)
    }

    @Test
    fun budgetComplianceRanksBestAndWorst() {
        val compliance = budgetCompliance(
            dataset(
                periods = listOf(
                    period(2026, 5, 1, "1000", "500"),
                    period(2026, 6, 1, "1000", "1200"),
                    period(2026, 7, 1, "1000", "800"),
                ),
            ),
        )
        assertEquals(50, compliance.bestPeriod!!.utilizationPercent)
        assertEquals(120, compliance.worstPeriod!!.utilizationPercent)
    }

    @Test
    fun budgetComplianceImportedPeriodHasNoUtilization() {
        val compliance = budgetCompliance(
            dataset(
                periods = listOf(period(2026, 5, 1, "0", "700", isImported = true)),
            ),
        )
        assertNull(compliance.periods[0].utilizationPercent)
        assertTrue(!compliance.periods[0].isOverspent)
        assertNull(compliance.bestPeriod)
        assertNull(compliance.worstPeriod)
    }

    @Test
    fun budgetComplianceCountsOverspendDaysAgainstDailyAllowance() {
        val compliance = budgetCompliance(
            dataset(
                periods = listOf(period(2026, 5, 1, "1000", "240")),
                spends = listOf(
                    spend(2026, 5, 1, "150", "Food"),
                    spend(2026, 5, 2, "90", "Food"),
                    spend(2026, 5, 3, "200", "Food"),
                ),
            ),
        )
        // 1000 budget over 28 days = ~35.71/day; all three day totals exceed it.
        assertEquals(3, compliance.periods[0].overspendDays)
    }

    // --- findAnomalies ------------------------------------------------------

    @Test
    fun anomaliesFlagOneOffBigTicket() {
        val anomalies = findAnomalies(
            dataset(
                spends = listOf(
                    spend(2026, 5, 15, "400", "Food"),
                    spend(2026, 6, 15, "400", "Food"),
                    spend(2026, 7, 15, "400", "Food"),
                    spend(2026, 8, 5, "900", "Food"),
                ),
            ),
        )
        val bigTicket = anomalies.firstOrNull { it.reason == AnomalyReason.ONE_OFF_BIG_TICKET }
        assertTrue("expected a big-ticket anomaly in $anomalies", bigTicket != null)
        assertAmount("900", bigTicket!!.amount)
        assertAmount("400", bigTicket.expected)
        assertAmount("800", bigTicket.threshold)
        assertEquals("food", bigTicket.category)
    }

    @Test
    fun anomaliesFlagWeekdayMedianBlowout() {
        val anomalies = findAnomalies(
            dataset(
                spends = listOf(
                    spend(2026, 7, 6, "100", "Food"),
                    spend(2026, 7, 13, "100", "Food"),
                    spend(2026, 7, 20, "100", "Food"),
                    spend(2026, 8, 10, "250", "Food"),
                ),
            ),
        )
        val weekday = anomalies.firstOrNull { it.reason == AnomalyReason.ABOVE_WEEKDAY_MEDIAN }
        assertTrue("expected a weekday-median anomaly in $anomalies", weekday != null)
        assertEquals(LocalDate.of(2026, Month.AUGUST, 10), weekday!!.date)
        assertAmount("250", weekday.amount)
        assertAmount("100", weekday.expected)
        assertAmount("200", weekday.threshold)
    }

    @Test
    fun anomaliesCappedAtFiveSortedByExcess() {
        val spends = mutableListOf<PatternSpend>()
        (1..6).forEach { day ->
            spends += spend(2026, 8, day, if (day <= 5) "1200" else "1100", "Food")
        }
        val anomalies = findAnomalies(
            dataset(
                spends = listOf(
                    spend(2026, 5, 15, "400", "Food"),
                    spend(2026, 6, 15, "400", "Food"),
                    spend(2026, 7, 15, "400", "Food"),
                ) + spends,
            ),
        )
        assertEquals(5, anomalies.size)
        val excess = anomalies.map { it.amount.subtract(it.expected) }
        assertEquals(excess.sortedDescending(), excess)
        assertTrue("expected excess 800 each but was $excess", excess.all { it.compareTo(BigDecimal("800")) == 0 })
    }

    @Test
    fun anomaliesEmptyDataset() {
        assertTrue(findAnomalies(dataset()).isEmpty())
    }

    // --- forecast -----------------------------------------------------------

    @Test
    fun forecastProjectsFromCompletedMonths() {
        val result = forecast(
            dataset(
                spends = listOf(
                    spend(2026, 5, 10, "100", "Food"),
                    spend(2026, 6, 10, "100", "Food"),
                    spend(2026, 7, 10, "100", "Food"),
                    spend(2026, 8, 2, "40", "Food"),
                ),
            ),
            dailyBudget = BigDecimal.ZERO,
        )
        assertAmount("100", result.monthlyAverage!!)
        assertEquals(0, result.trendPercent)
        assertAmount("88.45", result.projectedThisMonth!!)
        assertAmount("100", result.nextMonth!!)
        assertNull(result.pace)
    }

    @Test
    fun forecastPaceLabelsAgainstDailyBudget() {
        val dataset = dataset(
            spends = listOf(
                spend(2026, 5, 10, "100", "Food"),
                spend(2026, 6, 10, "100", "Food"),
                spend(2026, 7, 10, "100", "Food"),
                spend(2026, 8, 2, "40", "Food"),
            ),
        )
        assertEquals(PaceLabel.OVER_BUDGET, forecast(dataset, BigDecimal("2")).pace)
        assertEquals(PaceLabel.AT_RISK, forecast(dataset, BigDecimal("3")).pace)
        assertEquals(PaceLabel.ON_TRACK, forecast(dataset, BigDecimal("5")).pace)
        assertEquals(PaceLabel.SAVING, forecast(dataset, BigDecimal("10")).pace)
    }

    @Test
    fun forecastWithoutHistory() {
        val result = forecast(dataset(), dailyBudget = BigDecimal.ZERO)
        assertNull(result.monthlyAverage)
        assertNull(result.projectedThisMonth)
        assertNull(result.nextMonth)
        assertEquals(0, result.trendPercent)
    }

    // --- recurringCharges ---------------------------------------------------

    @Test
    fun recurringChargesDetectAcrossVariantSpellings() {
        val charges = recurringCharges(
            listOf(
                charge(2026, 5, 3, "499", "Netflix"),
                charge(2026, 6, 3, "499", "netflix "),
                charge(2026, 7, 3, "499", "NETFLIX"),
            ),
        )
        assertEquals(1, charges.size)
        assertEquals("Netflix", charges[0].normalizedComment)
        assertAmount("499", charges[0].monthlyAmount)
        assertEquals(3, charges[0].monthsApart)
        assertEquals(LocalDate.of(2026, Month.JULY, 3), charges[0].lastDate)
    }

    @Test
    fun recurringChargesIgnoreNonRecurring() {
        assertTrue(recurringCharges(listOf(charge(2026, 5, 3, "499", "Netflix"))).isEmpty())
        assertTrue(
            recurringCharges(
                listOf(
                    charge(2026, 5, 3, "499", "Netflix"),
                    charge(2026, 6, 3, "499", "Netflix"),
                ),
            ).isEmpty(),
        )
        assertTrue(recurringCharges(emptyList()).isEmpty())
    }

    @Test
    fun recurringChargesDropDriftedAmounts() {
        val charges = recurringCharges(
            listOf(
                charge(2026, 5, 3, "499", "Netflix"),
                charge(2026, 6, 3, "499", "Netflix"),
                charge(2026, 7, 3, "1000", "Netflix"),
            ),
        )
        assertTrue(charges.isEmpty())
    }

    private fun charge(year: Int, month: Int, day: Int, amount: String, comment: String): PatternCharge =
        PatternCharge(date = date(year, month, day), amount = BigDecimal(amount), comment = comment)

    // --- buildSuggestions ---------------------------------------------------

    private fun concentratedDataset(): PatternDataset = dataset(
        spends = listOf(
            spend(2026, 5, 1, "600", "Food"),
            spend(2026, 5, 2, "100", "Travel"),
            spend(2026, 5, 3, "100", "Other"),
            spend(2026, 6, 1, "600", "Food"),
            spend(2026, 6, 2, "100", "Travel"),
            spend(2026, 6, 3, "100", "Other"),
        ),
    )

    @Test
    fun suggestionsFlagDominantCategory() {
        val suggestions = buildSuggestions(concentratedDataset(), forecast(concentratedDataset(), BigDecimal.ZERO))
        val top = suggestions.firstOrNull { it.categoryKey == "food" && it.severity == Severity.HIGH }
        assertTrue("expected a dominant-category suggestion in $suggestions", top != null)
        assertTrue(top!!.actionable)
    }

    @Test
    fun suggestionsFlagTightBudget() {
        val dataset = dataset(
            periods = listOf(
                period(2026, 4, 1, "1000", "1200"),
                period(2026, 5, 1, "1000", "1100"),
                period(2026, 6, 1, "1000", "1050"),
            ),
        )
        val suggestions = buildSuggestions(dataset, forecast(dataset, BigDecimal.ZERO))
        assertTrue(suggestions.any { it.title == "Your budget may be too tight" })
    }

    @Test
    fun suggestionsFlagWeekendOverspend() {
        val dataset = dataset(
            spends = listOf(
                spend(2026, 8, 10, "100", "Food"),
                spend(2026, 8, 11, "100", "Food"),
                spend(2026, 8, 12, "100", "Food"),
                spend(2026, 8, 13, "100", "Food"),
                spend(2026, 8, 14, "100", "Food"),
                spend(2026, 8, 15, "300", "Food"),
                spend(2026, 8, 16, "300", "Food"),
            ),
        )
        val suggestions = buildSuggestions(dataset, forecast(dataset, BigDecimal.ZERO))
        assertTrue(suggestions.any { it.title == "Weekends cost more" })
    }

    @Test
    fun suggestionsFlagNoSpendDiscipline() {
        val dataset = dataset(
            spends = listOf(
                spend(2026, 8, 1, "100", "Food"),
                spend(2026, 8, 16, "100", "Food"),
            ),
        )
        val suggestions = buildSuggestions(dataset, forecast(dataset, BigDecimal.ZERO))
        assertTrue(suggestions.any { it.title == "Good no-spend discipline" })
    }

    @Test
    fun suggestionsRankedBySeverityAndCapped() {
        val dataset = dataset(
            spends = listOf(
                spend(2026, 5, 1, "600", "Food"),
                spend(2026, 5, 2, "100", "Travel"),
                spend(2026, 5, 3, "100", "Other"),
                spend(2026, 6, 1, "600", "Food"),
                spend(2026, 6, 2, "100", "Travel"),
                spend(2026, 6, 3, "100", "Other"),
            ),
            periods = listOf(
                period(2026, 4, 1, "1000", "1200"),
                period(2026, 5, 1, "1000", "1100"),
                period(2026, 6, 1, "1000", "1050"),
            ),
        )
        val suggestions = buildSuggestions(dataset, forecast(dataset, BigDecimal.ZERO))
        assertTrue(suggestions.size <= 5)
        val ranks = suggestions.map { it.severity.ordinal }
        assertEquals(ranks.sorted(), ranks)
    }

    // --- buildPatternReport / analyzePatterns --------------------------------

    @Test
    fun buildPatternReportHasStableStructure() {
        val dataset = dataset(
            spends = listOf(
                spend(2026, 5, 10, "100", "Food"),
                spend(2026, 8, 2, "60", "Food"),
            ),
        )
        val forecast = forecast(dataset, BigDecimal.ZERO)
        val suggestions = buildSuggestions(dataset, forecast)
        val report = buildPatternReport(dataset, suggestions, forecast)
        assertTrue(report.startsWith("Over the last 2 months you spent 160 in total, averaging 80/month."))
        assertTrue(report.contains("\n\n• "))
        assertTrue(report.contains("Tip:"))
    }

    @Test
    fun buildPatternReportEmptyDataset() {
        val report = buildPatternReport(dataset(), emptyList(), forecast(dataset(), BigDecimal.ZERO))
        assertTrue(report.startsWith("No spending history yet"))
    }

    @Test
    fun analyzePatternsBundlesEveryResult() {
        val dataset = dataset(
            spends = listOf(
                spend(2026, 5, 10, "100", "Food"),
                spend(2026, 6, 10, "100", "Food"),
                spend(2026, 7, 10, "100", "Food"),
                spend(2026, 8, 2, "40", "Food"),
            ),
            periods = listOf(period(2026, 5, 1, "500", "100")),
        )
        val metrics = analyzePatterns(dataset, dailyBudget = BigDecimal.ZERO)
        assertEquals(4, metrics.monthlyPoints.size)
        assertEquals(TrendDirection.STABLE, metrics.trendDirection)
        assertEquals(1, metrics.categories.size)
        assertEquals(1, metrics.compliance.periods.size)
        assertTrue(metrics.report.isNotEmpty())
        assertTrue(metrics.forecast.monthlyAverage != null)
        assertTrue(metrics.forecast.projectedThisMonth != null)
        assertTrue(metrics.suggestions.isNotEmpty())
    }

    // --- categoryMonthlySeries -----------------------------------------------

    @Test
    fun categoryMonthlySeriesEmptyWithoutSpends() {
        assertTrue(categoryMonthlySeries(dataset()).isEmpty())
    }

    @Test
    fun categoryMonthlySeriesAlignsToSpendMonthsWithZeroFill() {
        val d = dataset(
            spends = listOf(
                spend(2026, 6, 3, "30", "Food"),
                spend(2026, 7, 3, "20", "Food"),
                spend(2026, 8, 3, "50", "Food"),
                spend(2026, 8, 4, "40", "Transport"),
            ),
        )
        val series = categoryMonthlySeries(d)
        assertEquals(2, series.size)
        val food = series.first { it.key == "food" }
        assertEquals(listOf("30", "20", "50"), food.points.map { it.toPlainString() })
        val transport = series.first { it.key == "transport" }
        assertEquals(listOf("0", "0", "40"), transport.points.map { it.toPlainString() })
    }

    @Test
    fun categoryMonthlySeriesMergesSpellingVariants() {
        val d = dataset(
            spends = listOf(
                spend(2026, 7, 3, "10", "Food"),
                spend(2026, 8, 3, "20", "food!"),
            ),
        )
        val series = categoryMonthlySeries(d)
        assertEquals(1, series.size)
        assertEquals(listOf("10", "20"), series.single().points.map { it.toPlainString() })
    }
}
