package com.danilkinkin.buckwheat.patterns

import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Date

// Everything the spending-patterns page needs to reason about, expressed as plain value
// models with no Android / Room / DataStore imports so the whole engine runs under JUnit on
// the desktop. All math uses BigDecimal with an explicit RoundingMode.

// One spend as the pattern engine sees it. Deliberately carries NO comment: this is the view
// that feeds the AI prompt builder (Phase 2), so it structurally cannot leak spend comments.
// `category` is the stored category string; the ViewModel resolves blank ones via the offline
// keyword categorizer before constructing this.
data class PatternSpend(
    val date: Date,
    val value: BigDecimal,
    val category: String?,
)

// One budget period (archived or current). The current period is the one whose range contains
// `dataset.today`; archived periods carry `isImported`.
data class PatternPeriod(
    val start: Date,
    val finish: Date,
    val budget: BigDecimal,
    val totalSpent: BigDecimal,
    val isImported: Boolean,
)

// The full input to every pattern metric: chronological spends, all periods (archived +
// current appended), the currency they were recorded in, and the analysis date.
data class PatternDataset(
    val spends: List<PatternSpend>,
    val periods: List<PatternPeriod>,
    val currencyCode: String,
    val today: LocalDate,
)

// A spend WITH its comment, fed ONLY to the recurring-charge detector. This is pure on-device
// data; it is never part of a PatternDataset, so the AI prompt builder cannot reach it.
data class PatternCharge(
    val date: Date,
    val amount: BigDecimal,
    val comment: String,
)

// The flexible month window the user picks on the page: any count 1..availableMonths, or "All".
data class PatternWindow(
    val months: Int,
    val allData: Boolean,
)

enum class TrendDirection { UP, DOWN, STABLE }

enum class AnomalyReason { ABOVE_3M_AVG, ABOVE_WEEKDAY_MEDIAN, ONE_OFF_BIG_TICKET }

enum class PaceLabel { OVER_BUDGET, AT_RISK, ON_TRACK, SAVING }

enum class Severity { HIGH, MEDIUM, LOW }

// One normalized category's pattern. `key` is the canonical normalized name the engine groups
// by (typos/spacing/case variants merged into one); `displayName` is the spelling the user
// typed most often. `monthCount` = months with any spend in the dataset; `activeMonths` =
// months where THIS category spent (monthlyAverage divides by activeMonths, so a quiet month
// never dilutes the average — same rule as CategoryAutoAssign).
data class CategoryPattern(
    val key: String,
    val displayName: String,
    val total: BigDecimal,
    val percent: Int,
    val monthlyAverage: BigDecimal,
    val trend: TrendDirection,
    val monthCount: Int,
    val activeMonths: Int,
)

// One month slice of the monthly-trend chart.
data class MonthlyPoint(
    val label: String,
    val spent: BigDecimal,
    val budget: BigDecimal?,
    val isCurrent: Boolean,
)

// One top category's per-month spend series for the sparkline card. Points align with the
// dataset's spend months (oldest first); months the category was quiet in are zero-filled.
data class CategoryMonthlySeries(
    val key: String,
    val displayName: String,
    val points: List<BigDecimal>,
)

// One weekday slice of the weekday-rhythm card. All seven weekdays are present (zero-filled)
// so the chart has stable slots.
data class DayOfWeekPoint(
    val day: DayOfWeek,
    val total: BigDecimal,
    val count: Int,
    val sharePercent: Int,
)

// One day-of-month slice (1..31, zero-filled) of the day-of-month rhythm chart.
data class DayOfMonthPoint(
    val dayOfMonth: Int,
    val total: BigDecimal,
    val count: Int,
)

data class BusiestDay(
    val date: LocalDate,
    val total: BigDecimal,
)

data class Anomaly(
    val date: LocalDate,
    val amount: BigDecimal,
    val category: String?,
    val expected: BigDecimal,
    val threshold: BigDecimal,
    val reason: AnomalyReason,
)

data class Forecast(
    val projectedThisMonth: BigDecimal?,
    val nextMonth: BigDecimal?,
    val monthlyAverage: BigDecimal?,
    val trendPercent: Int,
    // null when no daily budget is set, so the UI can fall back to a dash.
    val pace: PaceLabel?,
)

data class RecurringCharge(
    // Majority spelling the user typed for this subscription.
    val normalizedComment: String,
    val monthlyAmount: BigDecimal,
    val lastDate: LocalDate,
    // How many distinct months the charge appeared in.
    val monthsApart: Int,
)

data class ConcentrationIndex(
    val topCategory: String?,
    val topSharePercent: Int,
    // Sum of squared shares (shares as 0..1 fractions), scale 4.
    val herfindahl: BigDecimal,
)

data class PeriodCompliance(
    val start: LocalDate,
    val finish: LocalDate,
    val budget: BigDecimal,
    val spent: BigDecimal,
    // Whole percent of the budget used; null when the period has no budget (imported).
    val utilizationPercent: Int?,
    val isOverspent: Boolean,
    // Days inside [start, finish] (capped at today) whose total spend exceeded the period's
    // per-day budget allowance.
    val overspendDays: Int,
)

data class BudgetCompliance(
    val periods: List<PeriodCompliance>,
    val overspentCount: Int,
    val bestPeriod: PeriodCompliance?,
    val worstPeriod: PeriodCompliance?,
)

data class InsightSuggestion(
    val severity: Severity,
    val title: String,
    val body: String,
    // Canonical category key when the suggestion is about a specific category (drill-in).
    val categoryKey: String?,
    val actionable: Boolean,
)

// Every pure-engine result bundled for the ViewModel state, so the UI stays dumb.
data class PatternMetrics(
    val monthlyPoints: List<MonthlyPoint>,
    val trendDirection: TrendDirection,
    val trendPercent: Int,
    val categories: List<CategoryPattern>,
    val concentration: ConcentrationIndex,
    val weekdayPoints: List<DayOfWeekPoint>,
    val weekendDeltaPercent: Int?,
    val dayOfMonthPoints: List<DayOfMonthPoint>,
    val noSpendDays: Int,
    val busiestDay: BusiestDay?,
    val compliance: BudgetCompliance,
    val anomalies: List<Anomaly>,
    val forecast: Forecast,
    val recurring: List<RecurringCharge>,
    val suggestions: List<InsightSuggestion>,
    val report: String,
)
