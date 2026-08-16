package com.danilkinkin.buckwheat.patterns

import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.Normalizer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

// The pure pattern engine. No Android / Room / DataStore imports — everything operates on the
// plain value models in PatternData.kt so it runs under JUnit on the desktop. All math uses
// BigDecimal with an explicit RoundingMode.

// Canonical key that every blank/uncategorized spend groups under.
const val UNCATEGORIZED_KEY = "other"

private const val UNCATEGORIZED_DISPLAY = "Other"

// --- Step 0: name normalization & typo merging --------------------------------

// Trims edges, collapses internal whitespace, normalizes unicode (NFKC), lowercases with the
// invariant locale and strips trailing/duplicate punctuation so "food", "Food ", "  FOOD  "
// and "food!!" all normalize to the same token. Deterministic and locale-independent.
fun normalizeName(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return ""
    var name = Normalizer.normalize(trimmed, Normalizer.Form.NFKC)
    name = name.replace(Regex("\\s+"), " ")
    name = name.lowercase()
    name = name.replace(Regex("[!?.,-]+$"), "")
    name = name.replace(Regex("([!?.,-])\\1+"), "$1")
    return name.trimEnd()
}

// Classic Levenshtein distance (edit distance). Pure and deterministic.
fun levenshteinDistance(a: String, b: String): Int {
    if (a == b) return 0
    val m = a.length
    val n = b.length
    if (m == 0) return n
    if (n == 0) return m
    var previous = IntArray(n + 1) { it }
    var current = IntArray(n + 1)
    for (i in 1..m) {
        current[0] = i
        for (j in 1..n) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            current[j] = minOf(previous[j] + 1, current[j - 1] + 1, previous[j - 1] + cost)
        }
        val swap = previous
        previous = current
        current = swap
    }
    return previous[n]
}

// The resolved category-name table the engine groups by.
// - `canonicalToDisplay`: canonical normalized key -> the spelling the user typed most often.
// - `aliasToCanonical`: every normalized spelling -> its canonical key (variants included).
data class CategoryNameIndex(
    val canonicalToDisplay: Map<String, String>,
    val aliasToCanonical: Map<String, String>,
)

// Groups near-duplicate category spellings (Levenshtein distance <= 2 after normalization)
// into one canonical key. The largest group becomes the anchor so the majority spelling wins
// both as the canonical key and as the display name; a smaller variant is folded into the
// first anchor it is close enough to. This only affects analysis views — it never renames the
// user's saved categories or rewrites transactions.
fun categoryNameIndex(names: List<String>): CategoryNameIndex {
    val nonBlank = names.filter { it.isNotBlank() }
    if (nonBlank.isEmpty()) return CategoryNameIndex(emptyMap(), emptyMap())

    // normalized -> (raw spelling, occurrence count), in first-seen order.
    val groups = linkedMapOf<String, MutableList<Pair<String, Int>>>()
    nonBlank.groupingBy { it }.eachCount().forEach { (raw, count) ->
        val norm = normalizeName(raw)
        if (norm.isNotEmpty()) groups.getOrPut(norm) { mutableListOf() } += raw to count
    }
    if (groups.isEmpty()) return CategoryNameIndex(emptyMap(), emptyMap())

    // Anchors are the biggest groups first, so a genuinely distinct category never gets
    // absorbed by a smaller neighbor and the majority spelling stays canonical.
    val ordered = groups.entries.sortedWith(
        compareByDescending<Map.Entry<String, List<Pair<String, Int>>>> { entry ->
            entry.value.sumOf { (_, count) -> count }
        }.thenBy { it.key },
    )
    val canonicalKeys = mutableListOf<String>()
    val aliasesByCanonical = linkedMapOf<String, MutableList<String>>()
    ordered.forEach { (norm, _) ->
        val anchor = canonicalKeys.firstOrNull { levenshteinDistance(norm, it) <= 2 }
        if (anchor != null) {
            aliasesByCanonical.getValue(anchor) += norm
        } else {
            canonicalKeys += norm
            aliasesByCanonical[norm] = mutableListOf(norm)
        }
    }

    val aliasToCanonical = linkedMapOf<String, String>()
    aliasesByCanonical.forEach { (canonical, aliases) ->
        aliases.forEach { alias -> aliasToCanonical[alias] = canonical }
    }

    val canonicalToDisplay = linkedMapOf<String, String>()
    aliasesByCanonical.forEach { (canonical, aliases) ->
        val spellings = mutableMapOf<String, Int>()
        aliases.forEach { alias ->
            groups.getValue(alias).forEach { (raw, count) ->
                spellings[raw] = (spellings[raw] ?: 0) + count
            }
        }
        canonicalToDisplay[canonical] = spellings.maxByOrNull { it.value }?.key ?: canonical
    }

    return CategoryNameIndex(canonicalToDisplay, aliasToCanonical)
}

// The canonical-key -> display-name half of the index (the documented public entry point).
fun mergeCategoryVariants(names: List<String>): Map<String, String> =
    categoryNameIndex(names).canonicalToDisplay

// Resolves one stored category string to its canonical key; blank/null -> UNCATEGORIZED_KEY.
// Unknown spellings fall back to their own normalized token (they just won't have a pattern
// shared with anything else).
fun canonicalCategory(category: String?, index: CategoryNameIndex): String {
    val raw = category?.takeIf { it.isNotBlank() } ?: return UNCATEGORIZED_KEY
    val norm = normalizeName(raw)
    return index.aliasToCanonical[norm] ?: norm
}

// --- Step 0.5: analysis window -------------------------------------------------

// Months spanned between the oldest spend and today, inclusive; min 1 (the current month).
// Bounds the window stepper on the page (the user can pick any count up to this).
fun availableMonths(dataset: PatternDataset): Int {
    if (dataset.spends.isEmpty()) return 1
    val oldest = YearMonth.from(dataset.spends.minOf { it.date.toLocalDate() })
    return (ChronoUnit.MONTHS.between(oldest, YearMonth.from(dataset.today)) + 1)
        .coerceAtLeast(1)
        .toInt()
}

// Inclusive Date threshold below which spends/periods fall outside the window; null for All.
// Uses the first day of the chosen month, so `months` means "the last N calendar months".
fun windowStartDate(dataset: PatternDataset, window: PatternWindow): Date? {
    if (window.allData) return null
    val months = window.months.coerceAtLeast(1)
    val startMonth = YearMonth.from(dataset.today).minusMonths((months - 1).toLong())
    return startMonth.atDay(1).toDate()
}

// Windows the dataset for analysis: drops spends older than the window start, and drops
// periods that ended before it (the current period always overlaps, so its budget still feeds
// the current month and the forecast). Keeping periods window-consistent means the monthly
// chart and the compliance card never show months outside the chosen range.
fun applyWindow(dataset: PatternDataset, window: PatternWindow): PatternDataset {
    val start = windowStartDate(dataset, window) ?: return dataset
    return dataset.copy(
        spends = dataset.spends.filter { !it.date.before(start) },
        periods = dataset.periods.filter { !it.finish.before(start) },
    )
}

// --- Step 1: monthly trend ----------------------------------------------------

private fun PatternPeriod.contains(day: LocalDate): Boolean =
    !day.isBefore(this.start.toLocalDate()) && !day.isAfter(this.finish.toLocalDate())

private fun monthFormatterFor(months: List<YearMonth>): DateTimeFormatter {
    val pattern = if (months.map { it.year }.distinct().size > 1) "MMM ''yy" else "MMM"
    return DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
}

// Months with any spend (or a period that started then with real data), oldest first, the
// current (partial) month last and flagged. Budget is the matching period's when it is set.
fun monthlyTotals(dataset: PatternDataset): List<MonthlyPoint> {
    val today = dataset.today
    val currentMonth = YearMonth.from(today)
    val zero = BigDecimal.ZERO
    val spentByMonth = dataset.spends
        .groupBy { YearMonth.from(it.date.toLocalDate()) }
        .mapValues { (_, monthSpends) ->
            monthSpends.fold(zero) { acc, spend -> acc + spend.value }
        }
    val periodMonths = dataset.periods
        .filter { it.budget > zero || it.totalSpent > zero }
        .map { YearMonth.from(it.start.toLocalDate()) }
    val months = (spentByMonth.keys + periodMonths).distinct().sorted()
    if (months.isEmpty()) return emptyList()

    val formatter = monthFormatterFor(months)

    return months.map { month ->
        MonthlyPoint(
            label = month.format(formatter),
            spent = spentByMonth[month] ?: zero,
            budget = budgetForMonth(dataset, month, today, currentMonth),
            isCurrent = month == currentMonth,
        )
    }
}

private fun budgetForMonth(
    dataset: PatternDataset,
    month: YearMonth,
    today: LocalDate,
    currentMonth: YearMonth,
): BigDecimal? {
    if (month == currentMonth) {
        return dataset.periods.firstOrNull { it.contains(today) }?.budget
            ?.takeIf { it > BigDecimal.ZERO }
    }
    return dataset.periods.firstOrNull { YearMonth.from(it.start.toLocalDate()) == month }
        ?.budget
        ?.takeIf { it > BigDecimal.ZERO }
}

// Completed (non-current) points, so a partial month never skews trend math.
private fun completedPoints(points: List<MonthlyPoint>): List<MonthlyPoint> =
    points.filterNot { it.isCurrent }

// Linear slope sign over the last 3 completed months; STABLE when the slope is under 5% of the
// average month (a "flat" trend). Needs at least 2 completed months.
fun trendDirection(points: List<MonthlyPoint>): TrendDirection {
    val completed = completedPoints(points).takeLast(3)
    if (completed.size < 2) return TrendDirection.STABLE
    val n = completed.size
    val sumX = (n - 1) * n / 2
    val sumX2 = (n - 1) * n * (2 * n - 1) / 6
    var sumY = BigDecimal.ZERO
    var sumXY = BigDecimal.ZERO
    completed.forEachIndexed { index, point ->
        sumY += point.spent
        sumXY += point.spent.multiply(BigDecimal(index))
    }
    val denominator = n * sumX2 - sumX * sumX
    if (denominator <= 0) return TrendDirection.STABLE
    val numerator = n.toBigDecimal().multiply(sumXY) - sumX.toBigDecimal().multiply(sumY)
    val slope = numerator.divide(denominator.toBigDecimal(), 4, RoundingMode.HALF_UP)
    val average = sumY.divide(n.toBigDecimal(), 4, RoundingMode.HALF_UP)
    if (average <= BigDecimal.ZERO) return TrendDirection.STABLE
    val stability = average.multiply(BigDecimal("0.05"))
    return when {
        slope.abs() < stability -> TrendDirection.STABLE
        slope > BigDecimal.ZERO -> TrendDirection.UP
        else -> TrendDirection.DOWN
    }
}

// Relative change from `from` to `to`, rounded to a whole percent (HALF_EVEN). 0 when the
// base month had no spend, so callers never divide by zero.
fun trendPercent(from: BigDecimal, to: BigDecimal): Int {
    if (from <= BigDecimal.ZERO) return 0
    return to.subtract(from)
        .multiply(BigDecimal(100))
        .divide(from, 0, RoundingMode.HALF_EVEN)
        .toInt()
}

// --- Step 2: category analysis ------------------------------------------------

// Per-category pattern: total, share, monthly average over the months it was active, and a
// first-half vs second-half split trend. Sorted by total desc (tie: key asc).
fun categoryPatterns(dataset: PatternDataset): List<CategoryPattern> {
    if (dataset.spends.isEmpty()) return emptyList()
    val zero = BigDecimal.ZERO
    val names = dataset.spends.mapNotNull { it.category?.takeIf { c -> c.isNotBlank() } }.distinct()
    val index = categoryNameIndex(names)
    val allMonths = dataset.spends.map { YearMonth.from(it.date.toLocalDate()) }.distinct()
    val grandTotal = dataset.spends.fold(zero) { acc, spend -> acc + spend.value }

    return dataset.spends
        .groupBy { canonicalCategory(it.category, index) }
        .map { (key, groupSpends) ->
            val total = groupSpends.fold(zero) { acc, spend -> acc + spend.value }
            val percent = if (grandTotal > zero) {
                total.multiply(BigDecimal(100)).divide(grandTotal, 0, RoundingMode.HALF_UP).toInt()
            } else {
                0
            }
            val activeMonths = groupSpends
                .map { YearMonth.from(it.date.toLocalDate()) }
                .distinct()
                .size
            val monthlyAverage = if (activeMonths > 0) {
                total.divide(activeMonths.toBigDecimal(), 2, RoundingMode.HALF_UP)
            } else {
                total
            }
            CategoryPattern(
                key = key,
                displayName = index.canonicalToDisplay[key]
                    ?: if (key == UNCATEGORIZED_KEY) UNCATEGORIZED_DISPLAY else key,
                total = total,
                percent = percent,
                monthlyAverage = monthlyAverage,
                trend = categoryTrend(groupSpends),
                monthCount = allMonths.size,
                activeMonths = activeMonths,
            )
        }
        .sortedWith(compareByDescending<CategoryPattern> { it.total }.thenBy { it.key })
}

private fun categoryTrend(groupSpends: List<PatternSpend>): TrendDirection {
    val months = groupSpends.map { YearMonth.from(it.date.toLocalDate()) }.distinct().sorted()
    if (months.size < 2) return TrendDirection.STABLE
    val firstHalf = months.subList(0, months.size / 2).toSet()
    var firstTotal = BigDecimal.ZERO
    var secondTotal = BigDecimal.ZERO
    groupSpends.forEach { spend ->
        if (YearMonth.from(spend.date.toLocalDate()) in firstHalf) {
            firstTotal += spend.value
        } else {
            secondTotal += spend.value
        }
    }
    return when {
        secondTotal > firstTotal.multiply(BigDecimal("1.1")) -> TrendDirection.UP
        secondTotal < firstTotal.multiply(BigDecimal("0.9")) -> TrendDirection.DOWN
        else -> TrendDirection.STABLE
    }
}

// Per-month spend series for the top-N categories (by total), for the sparkline card. The
// series aligns to the dataset's spend months (oldest first) with quiet months zero-filled.
fun categoryMonthlySeries(dataset: PatternDataset, top: Int = 4): List<CategoryMonthlySeries> {
    if (dataset.spends.isEmpty()) return emptyList()
    val categories = categoryPatterns(dataset).take(top)
    if (categories.isEmpty()) return emptyList()
    val months = dataset.spends.map { YearMonth.from(it.date.toLocalDate()) }.distinct().sorted()
    val names = dataset.spends.mapNotNull { it.category?.takeIf { c -> c.isNotBlank() } }.distinct()
    val index = categoryNameIndex(names)
    return categories.map { category ->
        val groupSpends = dataset.spends
            .filter { canonicalCategory(it.category, index) == category.key }
            .groupBy { YearMonth.from(it.date.toLocalDate()) }
        CategoryMonthlySeries(
            key = category.key,
            displayName = category.displayName,
            points = months.map { month ->
                groupSpends[month]?.fold(BigDecimal.ZERO) { acc, spend -> acc + spend.value }
                    ?: BigDecimal.ZERO
            },
        )
    }
}

// Spend concentration: top category + Herfindahl index (sum of squared 0..1 shares).
fun concentrationIndex(dataset: PatternDataset): ConcentrationIndex {
    if (dataset.spends.isEmpty()) return ConcentrationIndex(null, 0, BigDecimal.ZERO)
    val grandTotal = dataset.spends.fold(BigDecimal.ZERO) { acc, spend -> acc + spend.value }
    if (grandTotal <= BigDecimal.ZERO) return ConcentrationIndex(null, 0, BigDecimal.ZERO)
    val categories = categoryPatterns(dataset)
    val herfindahl = categories.fold(BigDecimal.ZERO) { acc, category ->
        val share = category.total.divide(grandTotal, 4, RoundingMode.HALF_UP)
        acc + share.multiply(share)
    }.setScale(4, RoundingMode.HALF_UP)
    val top = categories.firstOrNull()
    return ConcentrationIndex(
        topCategory = top?.displayName,
        topSharePercent = top?.percent ?: 0,
        herfindahl = herfindahl,
    )
}

// --- Step 3: calendar rhythms -------------------------------------------------

// All seven weekdays present (zero-filled) so the chart has stable slots.
fun weekdayPatterns(dataset: PatternDataset): List<DayOfWeekPoint> {
    val zero = BigDecimal.ZERO
    val grandTotal = dataset.spends.fold(zero) { acc, spend -> acc + spend.value }
    val byDay = dataset.spends.groupBy { it.date.toLocalDate().dayOfWeek }
    return DayOfWeek.entries.map { day ->
        val daySpends = byDay[day].orEmpty()
        val total = daySpends.fold(zero) { acc, spend -> acc + spend.value }
        val sharePercent = if (grandTotal > zero) {
            total.multiply(BigDecimal(100)).divide(grandTotal, 0, RoundingMode.HALF_UP).toInt()
        } else {
            0
        }
        DayOfWeekPoint(day = day, total = total, count = daySpends.size, sharePercent = sharePercent)
    }
}

// (weekend avg/day - weekday avg/day) as a whole percent over [oldest spend, today]. null when
// there is no weekday spend to compare against.
fun weekendVsWeekdayDelta(dataset: PatternDataset): Int? {
    val spends = dataset.spends
    if (spends.isEmpty()) return null
    val oldest = spends.minOf { it.date.toLocalDate() }
    val today = dataset.today
    if (today.isBefore(oldest)) return null

    var weekendDays = 0L
    var weekdayDays = 0L
    var cursor = oldest
    while (!cursor.isAfter(today)) {
        if (cursor.dayOfWeek == DayOfWeek.SATURDAY || cursor.dayOfWeek == DayOfWeek.SUNDAY) {
            weekendDays++
        } else {
            weekdayDays++
        }
        cursor = cursor.plusDays(1)
    }
    if (weekdayDays == 0L) return null

    var weekendTotal = BigDecimal.ZERO
    var weekdayTotal = BigDecimal.ZERO
    spends.forEach { spend ->
        val dayOfWeek = spend.date.toLocalDate().dayOfWeek
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            weekendTotal += spend.value
        } else {
            weekdayTotal += spend.value
        }
    }

    val weekdayAverage = weekdayTotal.divide(weekdayDays.toBigDecimal(), 4, RoundingMode.HALF_UP)
    if (weekdayAverage <= BigDecimal.ZERO) return null
    val weekendAverage = weekendTotal.divide(weekendDays.coerceAtLeast(1L).toBigDecimal(), 4, RoundingMode.HALF_UP)
    return weekendAverage.subtract(weekdayAverage)
        .multiply(BigDecimal(100))
        .divide(weekdayAverage, 0, RoundingMode.HALF_EVEN)
        .toInt()
}

// Days 1..31 of the month, zero-filled, so the rhythm chart can render a stable 31-bar strip
// (and the UI can group them into payday buckets).
fun dayOfMonthPattern(dataset: PatternDataset): List<DayOfMonthPoint> {
    val byDay = dataset.spends.groupBy { it.date.toLocalDate().dayOfMonth }
    return (1..31).map { day ->
        val daySpends = byDay[day].orEmpty()
        DayOfMonthPoint(
            dayOfMonth = day,
            total = daySpends.fold(BigDecimal.ZERO) { acc, spend -> acc + spend.value },
            count = daySpends.size,
        )
    }
}

// Calendar days inside [oldest spend, today] with no SPENT record at all.
fun noSpendDays(dataset: PatternDataset): Int {
    val spends = dataset.spends
    if (spends.isEmpty()) return 0
    val oldest = spends.minOf { it.date.toLocalDate() }
    val today = dataset.today
    if (today.isBefore(oldest)) return 0
    val totalDays = (ChronoUnit.DAYS.between(oldest, today) + 1).toInt()
    val spendDays = spends.map { it.date.toLocalDate() }.distinct().size
    return (totalDays - spendDays).coerceAtLeast(0)
}

// The single highest-total day.
fun busiestDay(dataset: PatternDataset): BusiestDay? =
    dataset.spends
        .groupBy { it.date.toLocalDate() }
        .mapValues { (_, daySpends) ->
            daySpends.fold(BigDecimal.ZERO) { acc, spend -> acc + spend.value }
        }
        .maxByOrNull { it.value }
        ?.let { BusiestDay(date = it.key, total = it.value) }

// --- Step 4: budget compliance ------------------------------------------------

// Per completed (archived) period: utilization, over/under flag, and overspend-day count
// (days whose total exceeded the period's per-day allowance, capped at today). The current
// (partial) period is excluded. Best/worst = lowest/highest utilization among budgeted rows.
fun budgetCompliance(dataset: PatternDataset): BudgetCompliance {
    val today = dataset.today
    val zero = BigDecimal.ZERO
    val rows = dataset.periods
        .filterNot { period ->
            val start = period.start.toLocalDate()
            val finish = period.finish.toLocalDate()
            !start.isAfter(today) && !finish.isBefore(today)
        }
        .map { period ->
            val start = period.start.toLocalDate()
            val finish = period.finish.toLocalDate()
            val utilizationPercent = if (period.budget > zero) {
                period.totalSpent.multiply(BigDecimal(100))
                    .divide(period.budget, 0, RoundingMode.HALF_UP)
                    .toInt()
            } else {
                null
            }
            val periodDays = (ChronoUnit.DAYS.between(start, finish) + 1).toInt().coerceAtLeast(1)
            val dailyBudget = if (period.budget > zero) {
                period.budget.divide(periodDays.toBigDecimal(), 2, RoundingMode.HALF_UP)
            } else {
                zero
            }
            PeriodCompliance(
                start = start,
                finish = finish,
                budget = period.budget,
                spent = period.totalSpent,
                utilizationPercent = utilizationPercent,
                isOverspent = period.budget > zero && period.totalSpent > period.budget,
                overspendDays = overspendDayCount(
                    spends = dataset.spends,
                    start = start,
                    finish = finish,
                    today = today,
                    dailyBudget = dailyBudget,
                ),
            )
        }
        .sortedBy { it.start }
    val withBudget = rows.filter { it.utilizationPercent != null }
    return BudgetCompliance(
        periods = rows,
        overspentCount = rows.count { it.isOverspent },
        bestPeriod = withBudget.minByOrNull { it.utilizationPercent ?: Int.MAX_VALUE },
        worstPeriod = withBudget.maxByOrNull { it.utilizationPercent ?: Int.MIN_VALUE },
    )
}

private fun overspendDayCount(
    spends: List<PatternSpend>,
    start: LocalDate,
    finish: LocalDate,
    today: LocalDate,
    dailyBudget: BigDecimal,
): Int {
    if (dailyBudget <= BigDecimal.ZERO) return 0
    val end = minOf(finish, today)
    if (end.isBefore(start)) return 0
    return spends
        .mapNotNull { spend ->
            spend.date.toLocalDate()
                .takeIf { day -> !day.isBefore(start) && !day.isAfter(end) }
                ?.let { day -> day to spend.value }
        }
        .groupBy({ it.first }, { it.second })
        .count { (_, values) ->
            values.fold(BigDecimal.ZERO) { acc, value -> acc + value } > dailyBudget
        }
}

// --- Step 5: anomalies --------------------------------------------------------

private fun median(values: List<BigDecimal>): BigDecimal {
    if (values.isEmpty()) return BigDecimal.ZERO
    val sorted = values.sorted()
    return sorted[sorted.size / 2]
}

// Anomalous spends/days, capped at the top 5 by excess (amount - expected).
// - ONE_OFF_BIG_TICKET: a single transaction above max(2 * monthlyMedian, monthlyAverage).
// - ABOVE_WEEKDAY_MEDIAN: a day above 2x the median day for its weekday (needs >= 3 samples).
// - ABOVE_3M_AVG: a day above the average completed month.
// A day is reported once; the weekday-median trigger wins over the month-average one.
fun findAnomalies(dataset: PatternDataset): List<Anomaly> {
    val spends = dataset.spends
    if (spends.isEmpty()) return emptyList()
    val zero = BigDecimal.ZERO

    val completedTotals = completedPoints(monthlyTotals(dataset)).takeLast(3).map { it.spent }
    val monthlyMean = if (completedTotals.isNotEmpty()) {
        completedTotals.fold(zero) { acc, value -> acc + value }
            .divide(completedTotals.size.toBigDecimal(), 4, RoundingMode.HALF_UP)
    } else {
        zero
    }
    val monthlyMedian = median(completedTotals)
    val names = spends.mapNotNull { it.category?.takeIf { c -> c.isNotBlank() } }.distinct()
    val index = categoryNameIndex(names)

    val anomalies = mutableListOf<Anomaly>()

    val bigTicketThreshold = maxOf(monthlyMedian.multiply(BigDecimal(2)), monthlyMean)
    if (bigTicketThreshold > zero) {
        spends.forEach { spend ->
            if (spend.value > bigTicketThreshold) {
                anomalies += Anomaly(
                    date = spend.date.toLocalDate(),
                    amount = spend.value,
                    category = canonicalCategory(spend.category, index),
                    expected = monthlyMean,
                    threshold = bigTicketThreshold,
                    reason = AnomalyReason.ONE_OFF_BIG_TICKET,
                )
            }
        }
    }

    val dailyTotals = spends
        .groupBy { it.date.toLocalDate() }
        .mapValues { (_, daySpends) ->
            daySpends.fold(zero) { acc, spend -> acc + spend.value }
        }
    val weekdayMedians = DayOfWeek.entries.associateWith { day ->
        val samples = dailyTotals.keys
            .filter { it.dayOfWeek == day }
            .map { dailyTotals.getValue(it) }
        if (samples.size >= 3) median(samples) else zero
    }
    dailyTotals.forEach { (day, total) ->
        val weekdayMedian = weekdayMedians.getValue(day.dayOfWeek)
        if (weekdayMedian > zero && total > weekdayMedian.multiply(BigDecimal(2))) {
            anomalies += Anomaly(
                date = day,
                amount = total,
                category = null,
                expected = weekdayMedian,
                threshold = weekdayMedian.multiply(BigDecimal(2)),
                reason = AnomalyReason.ABOVE_WEEKDAY_MEDIAN,
            )
        } else if (monthlyMean > zero && total > monthlyMean) {
            anomalies += Anomaly(
                date = day,
                amount = total,
                category = null,
                expected = monthlyMean,
                threshold = monthlyMean,
                reason = AnomalyReason.ABOVE_3M_AVG,
            )
        }
    }

    return anomalies
        .sortedWith(
            compareByDescending<Anomaly> { it.amount.subtract(it.expected) }
                .thenByDescending { it.date }
                .thenByDescending { it.amount },
        )
        .take(5)
}

// --- Step 6: forecast ---------------------------------------------------------

// Projects the current month and next month from the last 3 completed months, and labels the
// pace against the daily budget when one is set (null pace otherwise).
fun forecast(dataset: PatternDataset, dailyBudget: BigDecimal): Forecast {
    val completed = completedPoints(monthlyTotals(dataset)).takeLast(3)
    val monthlyAverage = if (completed.isNotEmpty()) {
        completed.fold(BigDecimal.ZERO) { acc, point -> acc + point.spent }
            .divide(completed.size.toBigDecimal(), 2, RoundingMode.HALF_UP)
    } else {
        null
    }
    val trendPercentValue = if (completed.size >= 2) {
        trendPercent(completed.first().spent, completed.last().spent)
    } else {
        0
    }

    val currentMonth = YearMonth.from(dataset.today)
    val currentSpent = dataset.spends
        .filter { YearMonth.from(it.date.toLocalDate()) == currentMonth }
        .fold(BigDecimal.ZERO) { acc, spend -> acc + spend.value }
    val daysInMonth = currentMonth.lengthOfMonth()
    val daysLeft = (daysInMonth - dataset.today.dayOfMonth).coerceAtLeast(0)
    val projectedThisMonth = monthlyAverage?.let { average ->
        val dailyProjection = average.divide(daysInMonth.toBigDecimal(), 2, RoundingMode.HALF_UP)
        currentSpent + dailyProjection.multiply(daysLeft.toBigDecimal()).setScale(2, RoundingMode.HALF_UP)
    }
    val nextMonth = monthlyAverage?.let { average ->
        val factor = BigDecimal.ONE.add(
            trendPercentValue.toBigDecimal().divide(BigDecimal(100), 4, RoundingMode.HALF_UP),
        )
        average.multiply(factor).setScale(2, RoundingMode.HALF_UP)
    }
    val pace = if (dailyBudget > BigDecimal.ZERO) {
        val monthlyBudget = dailyBudget.multiply(daysInMonth.toBigDecimal())
        val projected = projectedThisMonth ?: currentSpent
        when {
            projected > monthlyBudget -> PaceLabel.OVER_BUDGET
            projected >= monthlyBudget.multiply(BigDecimal("0.9")) -> PaceLabel.AT_RISK
            projected <= monthlyBudget.multiply(BigDecimal("0.5")) -> PaceLabel.SAVING
            else -> PaceLabel.ON_TRACK
        }
    } else {
        null
    }

    return Forecast(
        projectedThisMonth = projectedThisMonth,
        nextMonth = nextMonth,
        monthlyAverage = monthlyAverage,
        trendPercent = trendPercentValue,
        pace = pace,
    )
}

// --- Recurring charges (comment-carrying, on-device only) ---------------------

// Groups charges by their NORMALIZED comment (so "Netflix ", "netflix" and "Netflix" are one
// subscription) and keeps the group when the same comment + a similar amount (within 10% of
// the group median) appeared in 3+ distinct months. The display name is the majority spelling.
fun recurringCharges(charges: List<PatternCharge>): List<RecurringCharge> =
    charges
        .groupBy { normalizeName(it.comment) }
        .filterKeys { it.isNotEmpty() }
        .mapNotNull { (normalized, group) ->
            val amounts = group.map { it.amount }.sorted()
            val median = amounts[amounts.size / 2]
            if (median <= BigDecimal.ZERO) return@mapNotNull null
            val similar = group.filter { charge ->
                charge.amount.subtract(median).abs() <= median.multiply(BigDecimal("0.10"))
            }
            val months = similar.map { YearMonth.from(it.date.toLocalDate()) }.distinct().sorted()
            if (similar.isEmpty() || months.size < 3) return@mapNotNull null
            RecurringCharge(
                normalizedComment = majoritySpelling(group.map { it.comment }),
                monthlyAmount = median.setScale(2, RoundingMode.HALF_UP),
                lastDate = similar.maxOf { it.date.toLocalDate() },
                monthsApart = months.size,
            )
        }
        .sortedBy { it.normalizedComment }

private fun majoritySpelling(raws: List<String>): String =
    raws.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: ""

// --- Step 7: optimization suggestions -----------------------------------------

private val Severity.rank: Int
    get() = when (this) {
        Severity.HIGH -> 0
        Severity.MEDIUM -> 1
        Severity.LOW -> 2
    }

// Ranked, capped (5) optimization suggestions. HIGH first, then MEDIUM, then LOW.
fun buildSuggestions(
    dataset: PatternDataset,
    forecast: Forecast,
    recurring: List<RecurringCharge> = emptyList(),
): List<InsightSuggestion> {
    val suggestions = mutableListOf<InsightSuggestion>()
    val categories = categoryPatterns(dataset)

    categories.firstOrNull()?.let { top ->
        if (top.percent > 35) {
            val others = categories.filter { it.key != top.key }
            val body = if (others.isNotEmpty()) {
                val othersAverage = others.fold(BigDecimal.ZERO) { acc, category ->
                    acc + category.monthlyAverage
                }.divide(others.size.toBigDecimal(), 2, RoundingMode.HALF_UP)
                "${top.displayName} is ${top.percent}% of your spending. Trimming it to about " +
                    "${patternAmount(othersAverage)}/month (the average of your other categories) " +
                    "would balance your budget."
            } else {
                "${top.displayName} is ${top.percent}% of your spending — try trimming it."
            }
            suggestions += InsightSuggestion(
                severity = Severity.HIGH,
                title = "Cut back on ${top.displayName}",
                body = body,
                categoryKey = top.key,
                actionable = true,
            )
        }
    }

    val growthByCategory = categories
        .filter { it.trend == TrendDirection.UP }
        .associateWith { categoryGrowthRatio(dataset, it.key) }
    growthByCategory.maxByOrNull { it.value }?.let { (category, ratio) ->
        if (ratio > BigDecimal("1.1")) {
            suggestions += InsightSuggestion(
                severity = Severity.MEDIUM,
                title = "${category.displayName} is growing fastest",
                body = "${category.displayName} is your fastest-growing category — watch it or it " +
                    "will overtake your budget.",
                categoryKey = category.key,
                actionable = true,
            )
        }
    }

    weekendVsWeekdayDelta(dataset)?.let { delta ->
        if (delta > 50) {
            suggestions += InsightSuggestion(
                severity = Severity.MEDIUM,
                title = "Weekends cost more",
                body = "You spend about $delta% more per day on weekends than on weekdays.",
                categoryKey = null,
                actionable = false,
            )
        }
    }

    if (recurring.isNotEmpty()) {
        val topCharge = recurring.maxByOrNull { it.monthlyAmount }
        suggestions += InsightSuggestion(
            severity = Severity.MEDIUM,
            title = if (recurring.size == 1) {
                "A possible recurring charge"
            } else {
                "${recurring.size} possible recurring charges"
            },
            body = topCharge?.let {
                "e.g. ${it.normalizedComment} at ${patternAmount(it.monthlyAmount)}/month — check " +
                    "whether you still use them."
            } ?: "Review them to make sure they're all worth it.",
            categoryKey = null,
            actionable = false,
        )
    }

    val compliance = budgetCompliance(dataset)
    if (compliance.overspentCount > 0 && compliance.overspentCount > compliance.periods.size / 2) {
        suggestions += InsightSuggestion(
            severity = Severity.HIGH,
            title = "Your budget may be too tight",
            body = "You went over budget in ${compliance.overspentCount} of " +
                "${compliance.periods.size} periods. Consider adjusting it.",
            categoryKey = null,
            actionable = false,
        )
    }

    val concentration = concentrationIndex(dataset)
    if (concentration.herfindahl > BigDecimal("0.35") && concentration.topCategory != null) {
        suggestions += InsightSuggestion(
            severity = Severity.MEDIUM,
            title = "Spending is concentrated",
            body = "${concentration.topCategory} makes up ${concentration.topSharePercent}% of " +
                "your spending — a small trim there goes far.",
            categoryKey = null,
            actionable = false,
        )
    }

    val totalDays = totalDaysInRange(dataset)
    if (totalDays > 0) {
        val noSpendPercent = noSpendDays(dataset)
            .toBigDecimal()
            .multiply(BigDecimal(100))
            .divide(totalDays.toBigDecimal(), 0, RoundingMode.HALF_UP)
            .toInt()
        if (noSpendPercent >= 20) {
            suggestions += InsightSuggestion(
                severity = Severity.LOW,
                title = "Good no-spend discipline",
                body = "You had ${noSpendDays(dataset)} days with no spending. Keep it up — those " +
                    "quiet days are where savings happen.",
                categoryKey = null,
                actionable = false,
            )
        }
    }

    return suggestions
        .sortedWith(compareBy<InsightSuggestion> { it.severity.rank }.thenBy { it.title })
        .take(5)
}

private fun totalDaysInRange(dataset: PatternDataset): Int {
    val spends = dataset.spends
    if (spends.isEmpty()) return 0
    val oldest = spends.minOf { it.date.toLocalDate() }
    val today = dataset.today
    if (today.isBefore(oldest)) return 0
    return (ChronoUnit.DAYS.between(oldest, today) + 1).toInt()
}

private fun categoryGrowthRatio(dataset: PatternDataset, key: String): BigDecimal {
    val names = dataset.spends.mapNotNull { it.category?.takeIf { c -> c.isNotBlank() } }.distinct()
    val index = categoryNameIndex(names)
    val groupSpends = dataset.spends.filter { canonicalCategory(it.category, index) == key }
    val months = groupSpends.map { YearMonth.from(it.date.toLocalDate()) }.distinct().sorted()
    if (months.size < 2) return BigDecimal.ONE
    val firstHalf = months.subList(0, months.size / 2).toSet()
    var firstTotal = BigDecimal.ZERO
    var secondTotal = BigDecimal.ZERO
    groupSpends.forEach { spend ->
        if (YearMonth.from(spend.date.toLocalDate()) in firstHalf) {
            firstTotal += spend.value
        } else {
            secondTotal += spend.value
        }
    }
    if (firstTotal <= BigDecimal.ZERO) {
        return if (secondTotal > BigDecimal.ZERO) BigDecimal("2") else BigDecimal.ONE
    }
    return secondTotal.divide(firstTotal, 4, RoundingMode.HALF_UP)
}

// --- Step 8: offline narrative ------------------------------------------------

// Deterministic report in the same shape as the AI output (overview line, "• " bullets,
// conditional "Watch out for:" and "Tip:") so the AI/offline badge swap is seamless.
fun buildPatternReport(
    dataset: PatternDataset,
    suggestions: List<InsightSuggestion>,
    forecast: Forecast,
): String {
    val spends = dataset.spends
    if (spends.isEmpty()) {
        return "No spending history yet — once your first spends are in, the patterns page " +
            "will show your trends at a glance."
    }
    val months = spends.map { YearMonth.from(it.date.toLocalDate()) }.distinct().sorted()
    val grandTotal = spends.fold(BigDecimal.ZERO) { acc, spend -> acc + spend.value }
    val monthlyAverage = grandTotal.divide(months.size.toBigDecimal(), 2, RoundingMode.HALF_UP)
    val monthWord = if (months.size == 1) "month" else "months"

    val lines = mutableListOf<String>()
    lines += "Over the last ${months.size} $monthWord you spent ${patternAmount(grandTotal)} in " +
        "total, averaging ${patternAmount(monthlyAverage)}/month."

    lines += ""
    val bullets = mutableListOf<String>()
    suggestions.take(4).forEach { suggestion ->
        bullets += "• ${suggestion.title}: ${suggestion.body}"
    }
    forecast.projectedThisMonth?.let { projected ->
        bullets += "• If this pace holds, you'll spend about ${patternAmount(projected)} this month."
    }
    lines += bullets

    val pace = forecast.pace
    if (pace == PaceLabel.OVER_BUDGET || pace == PaceLabel.AT_RISK) {
        lines += ""
        lines += "Watch out for: you're heading over budget at the current pace."
    }

    suggestions.firstOrNull { it.severity == Severity.HIGH }?.let { top ->
        lines += ""
        lines += "Tip: ${top.body}"
    }

    return lines.joinToString("\n")
}

// Formats an amount the same way in the report and in tests: deterministic, locale-independent.
private fun patternAmount(value: BigDecimal): String =
    value.setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()

// --- Orchestration ------------------------------------------------------------

// Runs the whole engine and bundles every result into one PatternMetrics value.
fun analyzePatterns(
    dataset: PatternDataset,
    dailyBudget: BigDecimal,
    recurringCharges: List<PatternCharge> = emptyList(),
): PatternMetrics {
    val monthly = monthlyTotals(dataset)
    val completed = completedPoints(monthly)
    val forecast = forecast(dataset, dailyBudget)
    val recurring = recurringCharges(recurringCharges)
    val suggestions = buildSuggestions(dataset, forecast, recurring)
    return PatternMetrics(
        monthlyPoints = monthly,
        trendDirection = trendDirection(monthly),
        trendPercent = if (completed.size >= 2) {
            trendPercent(completed.first().spent, completed.last().spent)
        } else {
            0
        },
        categories = categoryPatterns(dataset),
        concentration = concentrationIndex(dataset),
        weekdayPoints = weekdayPatterns(dataset),
        weekendDeltaPercent = weekendVsWeekdayDelta(dataset),
        dayOfMonthPoints = dayOfMonthPattern(dataset),
        noSpendDays = noSpendDays(dataset),
        busiestDay = busiestDay(dataset),
        compliance = budgetCompliance(dataset),
        anomalies = findAnomalies(dataset),
        forecast = forecast,
        recurring = recurring,
        suggestions = suggestions,
        report = buildPatternReport(dataset, suggestions, forecast),
    )
}
