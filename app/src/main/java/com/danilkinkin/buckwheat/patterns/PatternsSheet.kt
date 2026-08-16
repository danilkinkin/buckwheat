package com.danilkinkin.buckwheat.patterns

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.analytics.CATEGORY_HISTORY_SHEET
import com.danilkinkin.buckwheat.analytics.MultiPeriodPoint
import com.danilkinkin.buckwheat.analytics.MultiPeriodTrendCard
import com.danilkinkin.buckwheat.analytics.SpendsWeekdayCard
import com.danilkinkin.buckwheat.analytics.categoriesChart.SpendCategoriesCard
import com.danilkinkin.buckwheat.base.AnimatedNumber
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.ui.colorBad
import com.danilkinkin.buckwheat.ui.colorGood
import com.danilkinkin.buckwheat.ui.colorMax
import com.danilkinkin.buckwheat.ui.colorMin
import com.danilkinkin.buckwheat.ui.colorNotGood
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.smoothPath
import com.danilkinkin.buckwheat.util.toDate
import java.math.BigDecimal
import java.math.RoundingMode

const val PATTERN_INSIGHTS_SHEET = "patterns"

// The full spending-patterns report. The pure engine always produces the charts + a narrative
// instantly; when AI is configured the narrative upgrades in the background (same swap-in as
// AiInsightSheet). The header is a decorative topo background with the window picker on top of
// the scrollable body.
@Composable
fun PatternsSheet(
    appViewModel: AppViewModel = hiltViewModel(),
    patternsViewModel: PatternsViewModel = hiltViewModel(),
) {
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current
    val state by patternsViewModel.state.observeAsState(PatternsUiState.Idle)
    val currentState = state

    val navigationBarHeight = androidx.compose.ui.unit.max(
        LocalWindowInsets.current.calculateBottomPadding(),
        16.dp,
    )

    LaunchedEffect(Unit) {
        if (patternsViewModel.state.value == PatternsUiState.Idle) {
            patternsViewModel.generate()
        }
    }

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp),
            ) {
                TopographyBackground(Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.patterns_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                        ),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = navigationBarHeight)
            ) {
                when (currentState) {
                    PatternsUiState.Idle, PatternsUiState.Loading -> LoadingBody()
                    is PatternsUiState.Error -> ErrorBody(currentState, patternsViewModel)
                    is PatternsUiState.Report -> PatternsBody(
                        report = currentState,
                        patternsViewModel = patternsViewModel,
                        appViewModel = appViewModel,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingBody() {
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.patterns_loading),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ErrorBody(error: PatternsUiState.Error, patternsViewModel: PatternsViewModel) {
    Text(
        text = error.message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )
    Spacer(Modifier.height(8.dp))
    TextButton(onClick = { patternsViewModel.generate() }) {
        Text(stringResource(R.string.patterns_retry))
    }
}

@Composable
private fun PatternsBody(
    report: PatternsUiState.Report,
    patternsViewModel: PatternsViewModel,
    appViewModel: AppViewModel,
) {
    val context = LocalContext.current
    val dataset = report.dataset
    val metrics = report.metrics
    val currency = ExtendCurrency.getInstance(dataset.currencyCode.takeIf { it.isNotBlank() })

    if (dataset.spends.isEmpty()) {
        Column(Modifier.fillMaxWidth()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Text(
                    text = stringResource(R.string.patterns_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            NarrativeCard(report, patternsViewModel)
        }
        return
    }

    val windowStart = windowStartDate(dataset, report.window) ?: dataset.today.toDate()
    val spendTx = remember(dataset) {
        dataset.spends.map {
            Transaction(
                type = TransactionType.SPENT,
                value = it.value,
                date = it.date,
                category = it.category,
            )
        }
    }

    val totalSpent = remember(metrics) {
        metrics.monthlyPoints.fold(BigDecimal.ZERO) { acc, point -> acc + point.spent }
    }
    val avgMonth = remember(totalSpent, metrics.monthlyPoints.size) {
        if (metrics.monthlyPoints.isEmpty()) {
            BigDecimal.ZERO
        } else {
            totalSpent.divide(BigDecimal(metrics.monthlyPoints.size), 2, RoundingMode.HALF_EVEN)
        }
    }

    val trendText: String
    val trendColor = when (metrics.trendDirection) {
        TrendDirection.UP -> {
            trendText = stringResource(R.string.patterns_trend_up, metrics.trendPercent)
            colorBad
        }
        TrendDirection.DOWN -> {
            trendText = stringResource(R.string.patterns_trend_down, metrics.trendPercent)
            colorGood
        }
        TrendDirection.STABLE -> {
            trendText = stringResource(R.string.patterns_trend_stable)
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    val categorySeries = remember(dataset) { categoryMonthlySeries(dataset) }

    Column(Modifier.fillMaxWidth()) {
        WindowSelector(report, onSetWindow = { patternsViewModel.setWindow(it) })

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(
                R.string.patterns_subtitle_range,
                prettyDate(
                    windowStart,
                    showTime = false,
                    forceShowDate = true,
                    shortMonth = true,
                ),
                prettyDate(
                    dataset.today.toDate(),
                    showTime = false,
                    forceShowDate = true,
                    shortMonth = true,
                ),
                if (report.window.allData) report.availableMonths else report.window.months,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(Modifier.fillMaxWidth()) {
                    KpiValue(
                        label = stringResource(R.string.patterns_total),
                        value = numberFormat(context, totalSpent, currency),
                        modifier = Modifier.weight(1f),
                    )
                    KpiValue(
                        label = stringResource(R.string.patterns_avg_month),
                        value = numberFormat(context, avgMonth, currency),
                        modifier = Modifier.weight(1f),
                    )
                    KpiValue(
                        label = stringResource(R.string.patterns_projected_now),
                        value = metrics.forecast.projectedThisMonth?.let {
                            numberFormat(context, it, currency)
                        } ?: "—",
                        modifier = Modifier.weight(1f),
                        alignEnd = true,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = trendColor.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = trendText,
                        style = MaterialTheme.typography.labelMedium,
                        color = trendColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        MultiPeriodTrendCard(
            modifier = Modifier.fillMaxWidth(),
            points = metrics.monthlyPoints.map {
                MultiPeriodPoint(
                    label = it.label,
                    spent = it.spent,
                    budget = it.budget ?: BigDecimal.ZERO,
                    isCurrent = it.isCurrent,
                )
            },
            currency = currency,
        )

        Spacer(Modifier.height(16.dp))
        SpendCategoriesCard(
            modifier = Modifier.fillMaxWidth(),
            spends = spendTx,
            currency = currency,
            onCategoryClick = { key ->
                appViewModel.openSheet(
                    PathState(
                        name = CATEGORY_HISTORY_SHEET,
                        args = mapOf("onlyCategoryKey" to key),
                    )
                )
            },
        )

        Spacer(Modifier.height(16.dp))
        CategorySparklines(categorySeries, currency)

        Spacer(Modifier.height(16.dp))
        SpendsWeekdayCard(
            modifier = Modifier.fillMaxWidth(),
            spends = spendTx,
            startDate = windowStart,
            finishDate = dataset.today.toDate(),
            currency = currency,
        )
        metrics.weekendDeltaPercent?.let { delta ->
            Spacer(Modifier.height(8.dp))
            val weekendText: String
            val weekendColor = when {
                delta > 0 -> {
                    weekendText = stringResource(R.string.patterns_weekend_delta_up, delta)
                    colorBad
                }
                delta < 0 -> {
                    weekendText = stringResource(R.string.patterns_weekend_delta_down, -delta)
                    colorGood
                }
                else -> {
                    weekendText = stringResource(R.string.patterns_weekend_delta_flat)
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            }
            Text(
                text = weekendText,
                style = MaterialTheme.typography.labelMedium,
                color = weekendColor,
            )
        }

        Spacer(Modifier.height(16.dp))
        DayOfMonthBars(metrics.dayOfMonthPoints, context, currency)

        Spacer(Modifier.height(16.dp))
        ComplianceCard(metrics, context, currency)

        Spacer(Modifier.height(16.dp))
        AnomaliesCard(metrics, context, currency)

        if (metrics.suggestions.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            SuggestionsCard(metrics.suggestions)
        }

        Spacer(Modifier.height(16.dp))
        NarrativeCard(report, patternsViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WindowSelector(
    report: PatternsUiState.Report,
    onSetWindow: (PatternWindow) -> Unit,
) {
    val preset = if (report.window.allData) null else report.window.months
    val current = if (report.window.allData) report.availableMonths else report.window.months

    Column(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(3, 6, 12).forEach { months ->
                FilterChip(
                    selected = preset == months,
                    onClick = { onSetWindow(PatternWindow(months, false)) },
                    label = {
                        Text(
                            stringResource(
                                when (months) {
                                    3 -> R.string.patterns_window_3m
                                    6 -> R.string.patterns_window_6m
                                    else -> R.string.patterns_window_12m
                                }
                            )
                        )
                    },
                )
            }
            FilterChip(
                selected = report.window.allData,
                onClick = { onSetWindow(PatternWindow(report.availableMonths, true)) },
                label = { Text(stringResource(R.string.patterns_window_all)) },
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { onSetWindow(PatternWindow((current - 1).coerceAtLeast(1), false)) },
                enabled = current > 1,
            ) {
                Text("−", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = if (report.window.allData) {
                    stringResource(R.string.patterns_window_all_label)
                } else {
                    pluralStringResource(R.plurals.patterns_window_last_months, current, current)
                },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = {
                    onSetWindow(
                        if (current >= report.availableMonths) {
                            PatternWindow(report.availableMonths, true)
                        } else {
                            PatternWindow(current + 1, false)
                        }
                    )
                },
                enabled = current < report.availableMonths,
            ) {
                Text("+", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun KpiValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        AnimatedNumber(
            value = value,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

// Top categories as a 3-column grid of mini area sparks (name + total + trend line).
@Composable
private fun CategorySparklines(
    series: List<CategoryMonthlySeries>,
    currency: ExtendCurrency,
) {
    if (series.isEmpty()) return
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.patterns_category_trends),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(16.dp))
            series.chunked(3).forEachIndexed { rowIndex, row ->
                if (rowIndex > 0) Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    val cells = row + List(3 - row.size) { null }
                    cells.forEachIndexed { index, item ->
                        if (index > 0) Spacer(Modifier.width(16.dp))
                        if (item != null) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = item.displayName,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = numberFormat(
                                        context,
                                        item.points.fold(BigDecimal.ZERO) { acc, point -> acc + point },
                                        currency,
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(8.dp))
                                MiniSpark(
                                    values = item.points,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                )
                            }
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

// Tiny gradient area line; normalizes to its own maximum so a quiet category still fills the cell.
@Composable
private fun MiniSpark(
    values: List<BigDecimal>,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val maxValue = values.maxOf { it }
        if (maxValue <= BigDecimal.ZERO) return@Canvas
        val topInset = 4.dp.toPx()
        val bottomInset = 2.dp.toPx()
        val chartBottom = size.height - bottomInset
        val chartHeight = (size.height - topInset - bottomInset).coerceAtLeast(1f)
        val slotWidth = size.width / (values.size - 1)

        fun yFor(value: BigDecimal): Float =
            chartBottom - chartHeight * value.divide(maxValue, 4, RoundingMode.HALF_EVEN).toFloat()

        val points = values.mapIndexed { index, value ->
            Offset(index * slotWidth, yFor(value))
        }
        val path = smoothPath(points)
        val area = Path().apply {
            addPath(path)
            lineTo(points.last().x, chartBottom)
            lineTo(points.first().x, chartBottom)
            close()
        }
        drawPath(
            path = area,
            brush = Brush.verticalGradient(
                0f to color.copy(alpha = 0.18f),
                1f to color.copy(alpha = 0.02f),
                startY = topInset,
                endY = chartBottom,
            ),
        )
        drawPath(path = path, color = color, style = Stroke(width = 2.dp.toPx()))
        drawCircle(color = color, radius = 2.5.dp.toPx(), center = points.last())
    }
}

// 31 slim bars (one per day of month) plus the 1-5 .. 26-31 bucket labels.
@Composable
private fun DayOfMonthBars(
    points: List<DayOfMonthPoint>,
    context: Context,
    currency: ExtendCurrency,
) {
    val maxValue = remember(points) { points.maxOfOrNull { it.total } ?: BigDecimal.ZERO }
    val busiest = points.maxByOrNull { it.total }
    val barColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.patterns_day_of_month),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
            ) {
                if (maxValue > BigDecimal.ZERO) {
                    val slotWidth = size.width / 31f
                    val barWidth = (slotWidth * 0.55f).coerceAtLeast(1.dp.toPx())
                    points.forEach { point ->
                        val index = point.dayOfMonth - 1
                        if (index in 0 until 31) {
                            val fraction = point.total
                                .divide(maxValue, 4, RoundingMode.HALF_EVEN)
                                .toFloat()
                            val barHeight = (size.height * fraction).coerceAtLeast(1.dp.toPx())
                            drawRoundRect(
                                color = barColor.copy(
                                    alpha = if (fraction > 0f) 1f else 0.08f,
                                ),
                                topLeft = Offset(
                                    index * slotWidth + (slotWidth - barWidth) / 2f,
                                    size.height - barHeight,
                                ),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                listOf(
                    1 to 5,
                    6 to 10,
                    11 to 15,
                    16 to 20,
                    21 to 25,
                    26 to 31,
                ).forEach { (from, to) ->
                    Text(
                        text = stringResource(R.string.patterns_day_bucket, from, to),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (busiest != null && busiest.total > BigDecimal.ZERO) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.patterns_day_of_month_busiest,
                        busiest.dayOfMonth,
                        numberFormat(context, busiest.total, currency),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ComplianceCard(
    metrics: PatternMetrics,
    context: Context,
    currency: ExtendCurrency,
) {
    val compliance = metrics.compliance

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.patterns_compliance),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.patterns_compliance_overspent,
                    compliance.overspentCount,
                    compliance.periods.size,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            compliance.periods.forEach { period ->
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "${prettyDate(period.start.toDate(), showTime = false, forceShowDate = true, shortMonth = true)} — ${prettyDate(period.finish.toDate(), showTime = false, forceShowDate = true, shortMonth = true)}",
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stringResource(
                                R.string.patterns_compliance_budget_to_spent,
                                numberFormat(context, period.budget, currency),
                                numberFormat(context, period.spent, currency),
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = period.utilizationPercent?.let { "$it%" } ?: "—",
                        style = MaterialTheme.typography.titleMedium,
                        color = when {
                            period.isOverspent -> colorBad
                            (period.utilizationPercent ?: 0) >= 80 -> colorNotGood
                            else -> colorGood
                        },
                    )
                }
            }
            compliance.bestPeriod?.let { best ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(
                        R.string.patterns_compliance_best,
                        best.utilizationPercent?.let { "$it%" } ?: "—",
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            compliance.worstPeriod?.let { worst ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.patterns_compliance_worst,
                        worst.utilizationPercent?.let { "$it%" } ?: "—",
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AnomaliesCard(
    metrics: PatternMetrics,
    context: Context,
    currency: ExtendCurrency,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.patterns_anomalies),
                style = MaterialTheme.typography.titleMedium,
            )
            if (metrics.anomalies.isEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.patterns_anomalies_none),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            metrics.anomalies.forEach { anomaly ->
                val reasonLabel: String
                val dotColor = when (anomaly.reason) {
                    AnomalyReason.ONE_OFF_BIG_TICKET -> {
                        reasonLabel = stringResource(R.string.patterns_anomaly_one_off)
                        colorMax
                    }
                    AnomalyReason.ABOVE_WEEKDAY_MEDIAN -> {
                        reasonLabel = stringResource(R.string.patterns_anomaly_weekday_median)
                        colorNotGood
                    }
                    AnomalyReason.ABOVE_3M_AVG -> {
                        reasonLabel = stringResource(R.string.patterns_anomaly_above_avg)
                        colorMin
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(dotColor, CircleShape),
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = prettyDate(
                                anomaly.date.toDate(),
                                showTime = false,
                                forceShowDate = true,
                                shortMonth = true,
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = reasonLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = numberFormat(context, anomaly.amount, currency),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                        )
                        Text(
                            text = stringResource(
                                R.string.patterns_anomaly_expected,
                                numberFormat(
                                    context,
                                    anomaly.expected,
                                    currency,
                                    maximumFractionDigits = 0,
                                    minimumFractionDigits = 0,
                                ),
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionsCard(suggestions: List<InsightSuggestion>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.patterns_optimize),
                style = MaterialTheme.typography.titleMedium,
            )
            suggestions.forEach { suggestion ->
                val severityLabel: String
                val severityColor = when (suggestion.severity) {
                    Severity.HIGH -> {
                        severityLabel = stringResource(R.string.patterns_severity_high)
                        colorBad
                    }
                    Severity.MEDIUM -> {
                        severityLabel = stringResource(R.string.patterns_severity_medium)
                        colorNotGood
                    }
                    Severity.LOW -> {
                        severityLabel = stringResource(R.string.patterns_severity_low)
                        colorGood
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .width(4.dp)
                            .height(42.dp)
                            .background(severityColor, RoundedCornerShape(2.dp)),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = severityLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = severityColor,
                        )
                        Text(
                            text = suggestion.title,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        if (suggestion.body.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = suggestion.body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

// The narrative card: an AI/Offline badge, an optional "improving with AI" / AI-failure caption,
// a regenerate button, then the report body rendered as scannable bullets/paragraphs.
@Composable
private fun NarrativeCard(
    report: PatternsUiState.Report,
    patternsViewModel: PatternsViewModel,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        if (report.isAi) R.string.patterns_ai_badge_ai
                        else R.string.patterns_ai_badge_offline
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.weight(1f))
                if (report.aiLoading) {
                    Text(
                        text = stringResource(R.string.patterns_ai_loading),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    TextButton(onClick = { patternsViewModel.generate(report.window) }) {
                        Text(stringResource(R.string.patterns_regenerate))
                    }
                }
            }
            if (!report.isAi && report.aiFailure != null && !report.aiLoading) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.patterns_ai_failed, report.aiFailure),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            ReportBody(report.narrative)
        }
    }
}

// Renders the narrative line by line: "• " bullet lines become bullet rows, everything else a
// paragraph. Mirrors the monthly-report body so both reports read the same way.
@Composable
private fun ReportBody(text: String) {
    val lines = remember(text) { text.lines() }
    Column(Modifier.fillMaxWidth()) {
        lines.forEach { rawLine ->
            val line = rawLine.trim()
            when {
                line.isEmpty() -> Spacer(Modifier.height(6.dp))
                line.startsWith("•") -> {
                    Row(Modifier.padding(top = 4.dp)) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = line.removePrefix("•").trim(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                else -> Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
