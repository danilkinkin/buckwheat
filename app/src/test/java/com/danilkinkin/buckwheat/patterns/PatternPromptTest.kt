package com.danilkinkin.buckwheat.patterns

import com.danilkinkin.buckwheat.ai.AiInsightResult
import com.danilkinkin.buckwheat.ai.AiRouterResult
import com.danilkinkin.buckwheat.util.toDate
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Deterministic prompt-layer tests: fixtures are fixed LocalDates, expectations exact.
class PatternPromptTest {
    private fun date(year: Int, month: Int, day: Int): Date = LocalDate.of(year, month, day).toDate()

    private fun spend(year: Int, month: Int, day: Int, value: String, category: String?): PatternSpend =
        PatternSpend(date = date(year, month, day), value = BigDecimal(value), category = category)

    private fun dataset(
        today: LocalDate = LocalDate.of(2026, Month.AUGUST, 16),
        spends: List<PatternSpend>,
    ): PatternDataset = PatternDataset(
        spends = spends,
        periods = emptyList(),
        currencyCode = "USD",
        today = today,
    )

    // A three-month history with a dominant, growing "food" category (with a typo variant) so
    // the summary exercises normalization + category aggregation + anomalies.
    private fun threeMonthSpends(): List<PatternSpend> = listOf(
        spend(2026, 5, 5, "100", "Food"),
        spend(2026, 5, 6, "50", "food!"),
        spend(2026, 6, 7, "120", "Food"),
        spend(2026, 6, 21, "60", "Travel"),
        spend(2026, 7, 8, "150", "Food"),
        spend(2026, 7, 9, "400", "Food"),
        spend(2026, 8, 2, "30", "Travel"),
        spend(2026, 8, 10, "90", "Food"),
    )

    private fun summary(spends: List<PatternSpend>): PatternAiSummary {
        val dataset = dataset(spends = spends)
        return buildPatternAiSummary(dataset, analyzePatterns(dataset, BigDecimal.ZERO))
    }

    // --- buildPatternAiSystemPrompt ---

    @Test
    fun systemPromptSetsTheAnalystPersonaAndRules() {
        val prompt = buildPatternAiSystemPrompt()
        assertTrue(prompt.contains("personal finance analyst"))
        assertTrue(prompt.contains("language the user writes in"))
        assertTrue(prompt.contains("• "))
        assertTrue(prompt.contains("Never invent transactions or numbers"))
        assertTrue(prompt.contains("160 words"))
        assertTrue(prompt.contains("Do not use markdown headers or code blocks"))
    }

    // --- buildPatternAiUserPrompt ---

    @Test
    fun userPromptContainsTheAggregateFacts() {
        val prompt = buildPatternAiUserPrompt(summary(threeMonthSpends()))
        assertTrue(prompt.contains("Currency: USD"))
        assertTrue(prompt.contains("4 months"))
        assertTrue(prompt.contains("avg "))
        assertTrue(prompt.contains("Monthly totals"))
        assertTrue(prompt.contains("Category breakdown"))
        assertTrue(prompt.contains("Budget compliance: 0 of 0 periods over budget"))
        assertTrue(prompt.contains("Anomalies"))
        assertTrue(prompt.contains("Forecast"))
        assertTrue(prompt.contains("Possible recurring charges: 0"))
    }

    @Test
    fun userPromptUsesTheNormalizedDisplayNameForVariantSpellings() {
        val prompt = buildPatternAiUserPrompt(summary(threeMonthSpends()))
        // "Food" + "food!" merge into one category; the majority spelling "Food" is displayed
        // and the variant's total is folded in.
        val foodLine = prompt.lineSequence().firstOrNull { it.contains("Food:") && it.contains("avg ") }
        assertTrue("expected a merged Food category line in:\n$prompt", foodLine != null)
        assertTrue("expected food to have merged the 100+50+120+150+400+90 = 910 total", foodLine!!.contains("910"))
        assertFalse("must not leak the raw variant spelling", prompt.contains("food!"))
    }

    @Test
    fun userPromptListsAnomaliesAsAmountsAndDatesOnly() {
        val prompt = buildPatternAiUserPrompt(summary(threeMonthSpends()))
        // The 400 spend on 2026-07-09 is a one-off big ticket against a ~150/month baseline.
        assertTrue("expected an anomaly line in:\n$prompt", prompt.contains("9 Jul 2026: 400"))
        assertFalse("must not mention comments (there are none to leak)", prompt.contains("comment"))
    }

    @Test
    fun userPromptReportsRecurringCountButNoNames() {
        val charges = listOf(
            PatternCharge(date(2026, 5, 3), BigDecimal("500"), "Netflix"),
            PatternCharge(date(2026, 6, 3), BigDecimal("500"), "netflix"),
            PatternCharge(date(2026, 7, 3), BigDecimal("499"), "Netflix"),
        )
        val dataset = dataset(spends = threeMonthSpends())
        val metrics = analyzePatterns(dataset, BigDecimal.ZERO, recurringCharges = charges)
        val prompt = buildPatternAiUserPrompt(buildPatternAiSummary(dataset, metrics))
        assertTrue("expected recurring count to reach the prompt", prompt.contains("Possible recurring charges: 1"))
        assertFalse("must not leak the comment-derived subscription name", prompt.contains("Netflix"))
        assertFalse("must not leak the comment-derived subscription name", prompt.contains("netflix"))
    }

    @Test
    fun userPromptOmitsTheRecurringSuggestion() {
        val charges = listOf(
            PatternCharge(date(2026, 5, 3), BigDecimal("500"), "Netflix"),
            PatternCharge(date(2026, 6, 3), BigDecimal("500"), "netflix"),
            PatternCharge(date(2026, 7, 3), BigDecimal("499"), "Netflix"),
        )
        val dataset = dataset(spends = threeMonthSpends())
        val metrics = analyzePatterns(dataset, BigDecimal.ZERO, recurringCharges = charges)
        val summary = buildPatternAiSummary(dataset, metrics)
        assertFalse(
            "recurring suggestion body embeds a comment-derived name and must be stripped",
            summary.suggestions.any { it.title.contains("recurring", ignoreCase = true) },
        )
    }

    @Test
    fun userPromptHandlesAnEmptyHistory() {
        val prompt = buildPatternAiUserPrompt(summary(emptyList()))
        assertTrue(prompt.contains("Currency: USD"))
        assertTrue(prompt.contains("0 months"))
        assertTrue(prompt.contains("Category breakdown"))
        assertTrue(prompt.contains("Monthly totals"))
    }

    // --- parsePatternAiReport ---

    @Test
    fun parserStripsFencesLabelsAndBlankLines() {
        val raw = "```\nReport: Overview looks fine.\n\n• Food is the biggest bucket.\n\n\n\n```"
        assertEquals(
            "Overview looks fine.\n\n• Food is the biggest bucket.",
            parsePatternAiReport(raw),
        )
    }

    @Test
    fun parserKeepsPlainTextUnchanged() {
        val raw = "You are on track. • Keep going."
        assertEquals(raw, parsePatternAiReport(raw))
    }

    // --- mapPatternAiResult ---

    @Test
    fun mapPatternAiResultMapsSuccessAndCleans() {
        val result = mapPatternAiResult(AiRouterResult.Success("```\nReport: Good.\n```"))
        assertEquals(AiInsightResult.Success("Good."), result)
    }

    @Test
    fun mapPatternAiResultTreatsEmptyReportAsFailure() {
        val result = mapPatternAiResult(AiRouterResult.Success("```\n```"))
        assertTrue(result is AiInsightResult.Failure)
    }

    @Test
    fun mapPatternAiResultPassesThroughFailureAndNotConfigured() {
        val failure = mapPatternAiResult(AiRouterResult.Failure("network down"))
        assertEquals(AiInsightResult.Failure("network down"), failure)
        assertEquals(AiInsightResult.NotConfigured, mapPatternAiResult(AiRouterResult.NotConfigured))
    }
}
