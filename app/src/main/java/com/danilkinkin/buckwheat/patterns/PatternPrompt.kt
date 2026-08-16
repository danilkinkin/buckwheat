package com.danilkinkin.buckwheat.patterns

import android.content.Context
import com.danilkinkin.buckwheat.ai.AiInsightResult
import com.danilkinkin.buckwheat.ai.AiRouterResult
import com.danilkinkin.buckwheat.ai.callAi
import com.danilkinkin.buckwheat.ai.parseAiInsightReport
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter

// The AI layer for the Spending Patterns page: an anonymous, privacy-safe aggregate of the
// pattern metrics, a prompt builder, a parser and the callAi wrapper. Same posture as
// SpendInsightSummary — no comment text is ever sent; recurring-charge names (which derive from
// comments) are excluded and only their COUNT reaches the model.
//
// PatternSpend carries no comment by construction (see PatternData.kt), and buildPatternAiSummary
// strips the recurring-charge suggestion (its body embeds the comment-derived name) before the
// aggregate is built, so nothing comment-derived can leak into the prompt.

private val PATTERN_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy")

// Formats an amount the same way in the prompt and in tests: deterministic, locale-independent.
internal fun patternPromptAmount(value: BigDecimal): String =
    value.setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()

// Anonymous, AI-safe view of the pattern metrics. Carries only totals, dates, category display
// names (the normalized majority spelling — never raw comment text), counts and sanitized
// suggestions.
data class PatternAiSummary(
    val currencyCode: String,
    val totalSpent: BigDecimal,
    val monthCount: Int,
    val months: List<MonthlyPoint>,
    val trendDirection: TrendDirection,
    val trendPercent: Int,
    val categories: List<CategoryPattern>,
    val weekendDeltaPercent: Int?,
    val compliance: BudgetCompliance,
    val anomalies: List<Anomaly>,
    val forecast: Forecast,
    val recurringCount: Int,
    val noSpendDays: Int,
    val suggestions: List<InsightSuggestion>,
)

// Builds the AI-safe aggregate from the pure engine's metrics. The recurring-charge suggestion
// is dropped here (its body embeds a comment-derived name like "Netflix") — the count is passed
// separately so the AI can still mention subscriptions without seeing their names.
fun buildPatternAiSummary(
    dataset: PatternDataset,
    metrics: PatternMetrics,
): PatternAiSummary {
    val totalSpent = metrics.monthlyPoints.fold(BigDecimal.ZERO) { acc, point -> acc + point.spent }
    return PatternAiSummary(
        currencyCode = dataset.currencyCode,
        totalSpent = totalSpent,
        monthCount = metrics.monthlyPoints.size,
        months = metrics.monthlyPoints,
        trendDirection = metrics.trendDirection,
        trendPercent = metrics.trendPercent,
        categories = metrics.categories,
        weekendDeltaPercent = metrics.weekendDeltaPercent,
        compliance = metrics.compliance,
        anomalies = metrics.anomalies,
        forecast = metrics.forecast,
        recurringCount = metrics.recurring.size,
        noSpendDays = metrics.noSpendDays,
        suggestions = metrics.suggestions.filterNot { suggestion ->
            suggestion.title.contains("recurring", ignoreCase = true)
        },
    )
}

internal fun buildPatternAiSystemPrompt(): String =
    "You are a personal finance analyst for the Buckwheat budget app. Analyze the user's " +
        "spending patterns across several months and reply with a concise, friendly analysis in " +
        "the language the user writes in. Structure your answer as: 1) one overview sentence on " +
        "the overall trend, 2) 3-5 bullet observations (start each line with '• '), calling out " +
        "category concentration, month-over-month trend, weekday/weekend rhythm and any anomalies, " +
        "3) a short 'Watch out for' note only if something needs attention, and 4) one practical " +
        "tip. Never invent transactions or numbers that are not in the data. Keep it under 160 " +
        "words. Do not use markdown headers or code blocks."

internal fun buildPatternAiUserPrompt(summary: PatternAiSummary): String {
    val monthWord = if (summary.monthCount == 1) "month" else "months"
    val monthLines = if (summary.months.isEmpty()) {
        "  none"
    } else {
        summary.months.joinToString("\n") { point ->
            val current = if (point.isCurrent) " (current)" else ""
            "  - ${point.label}: ${patternPromptAmount(point.spent)}$current"
        }
    }
    val categoryLines = if (summary.categories.isEmpty()) {
        "  none"
    } else {
        summary.categories.joinToString("\n") { category ->
            "  - ${category.displayName}: ${patternPromptAmount(category.total)} (${category.percent}%), " +
                "avg ${patternPromptAmount(category.monthlyAverage)}/month, " +
                "trend ${category.trend.name.lowercase()}"
        }
    }
    val anomalyLines = if (summary.anomalies.isEmpty()) {
        "  none"
    } else {
        summary.anomalies.joinToString("\n") { anomaly ->
            "  - ${anomaly.date.format(PATTERN_DATE_FORMAT)}: ${patternPromptAmount(anomaly.amount)} " +
                "(expected ${patternPromptAmount(anomaly.expected)}, ${anomaly.reason.name.lowercase()})"
        }
    }
    val suggestionLines = if (summary.suggestions.isEmpty()) {
        "  none"
    } else {
        summary.suggestions.joinToString("\n") { suggestion ->
            "  - [${suggestion.severity.name}] ${suggestion.title}: ${suggestion.body}"
        }
    }
    val trend = when {
        summary.trendDirection == TrendDirection.STABLE -> "stable"
        else -> "${summary.trendDirection.name.lowercase()} ${summary.trendPercent}%"
    }
    val weekend = summary.weekendDeltaPercent?.let { "$it% more per weekend day" } ?: "no clear difference"
    val monthlyAverage = if (summary.monthCount > 0) {
        summary.totalSpent.divide(summary.monthCount.toBigDecimal(), 2, RoundingMode.HALF_EVEN)
    } else {
        BigDecimal.ZERO
    }

    return buildString {
        append("Currency: ${summary.currencyCode.ifBlank { "(none)" }}")
        append(
            "\nPeriod covered: ${summary.monthCount} $monthWord, " +
                "${patternPromptAmount(summary.totalSpent)} total, " +
                "avg ${patternPromptAmount(monthlyAverage)}/month"
        )
        append("\nOverall trend: $trend")
        append("\nMonthly totals:\n")
        append(monthLines)
        append("\nCategory breakdown (normalized names):\n")
        append(categoryLines)
        append("\nWeekend vs weekday: $weekend")
        append("\nBudget compliance: ${summary.compliance.overspentCount} of " +
            "${summary.compliance.periods.size} periods over budget")
        append("\nAnomalies (amounts and dates only):\n")
        append(anomalyLines)
        append("\nForecast this month: " +
            "${summary.forecast.projectedThisMonth?.let { patternPromptAmount(it) } ?: "none"}")
        append("\nForecast next month: " +
            "${summary.forecast.nextMonth?.let { patternPromptAmount(it) } ?: "none"}")
        append("\nPossible recurring charges: ${summary.recurringCount} (names not sent)")
        append("\nNo-spend days: ${summary.noSpendDays}")
        append("\nCurrent suggestions:\n")
        append(suggestionLines)
    }
}

// Cleans the model's report for display: same cleanup as the monthly-report parser (strip
// markdown code fences, an echoed "Report:" label and excess blank lines). Pure so it is
// unit-testable.
internal fun parsePatternAiReport(raw: String): String = parseAiInsightReport(raw)

// Maps a backend result to the page's result type. Pure so it is unit-testable; a Success with
// an empty (post-cleanup) report is treated as a Failure.
internal fun mapPatternAiResult(result: AiRouterResult): AiInsightResult = when (result) {
    is AiRouterResult.Success -> {
        val report = parsePatternAiReport(result.text)
        if (report.isEmpty()) {
            AiInsightResult.Failure("response contained no analysis")
        } else {
            AiInsightResult.Success(report)
        }
    }
    is AiRouterResult.Failure -> AiInsightResult.Failure(result.message)
    AiRouterResult.NotConfigured -> AiInsightResult.NotConfigured
}

// Generates the AI pattern analysis through the shared backend. NotConfigured when the AI
// Intelligence toggle is off or no key is saved; Failure carries a human-readable reason.
suspend fun generatePatternAiInsight(
    context: Context,
    summary: PatternAiSummary,
): AiInsightResult = mapPatternAiResult(callAi(
    context = context,
    systemPrompt = buildPatternAiSystemPrompt(),
    userPrompt = buildPatternAiUserPrompt(summary),
))
