package com.danilkinkin.buckwheat.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SubscriptionsViewModel
import com.danilkinkin.buckwheat.data.billCategoryNames
import com.danilkinkin.buckwheat.base.CategoryIcon
import com.danilkinkin.buckwheat.base.DonutChart
import com.danilkinkin.buckwheat.base.DonutSegment
import com.danilkinkin.buckwheat.base.GradientCard
import com.danilkinkin.buckwheat.data.monthlyEquivalent
import com.danilkinkin.buckwheat.data.percentDelta
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.prettyYearMonth
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.roundToInt
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Ledger statistics: what a month cost, where it went and how it was spread over the days.
 * No budget, no period, no horizontal scrolling — one month per screen, read top-down.
 */
@Composable
fun StatsScreen(
    onBack: () -> Unit,
    onOpenSubscriptionInsights: () -> Unit = {},
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    subscriptionsViewModel: SubscriptionsViewModel = hiltViewModel(),
) {
    val spends by spendsViewModel.spends.observeAsState(emptyList())
    val categories by spendsViewModel.categories.observeAsState(emptyList())
    val subscriptions by subscriptionsViewModel.subscriptions.observeAsState(emptyList())
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val categoriesById = remember(categories) { categories.associateBy { it.id } }

    val today = LocalDate.now()
    val currentMonth = YearMonth.from(today)
    var month by remember { mutableStateOf(currentMonth) }

    val monthSpends = remember(spends, month) {
        spends.filter { YearMonth.from(it.date.toLocalDate()) == month }
    }
    val total = remember(monthSpends) {
        monthSpends.fold(BigDecimal.ZERO) { sum, spend -> sum + spend.value }
    }
    val previousMonth = month.minusMonths(1)
    val previousSpends = remember(spends, previousMonth) {
        spends.filter { YearMonth.from(it.date.toLocalDate()) == previousMonth }
    }
    val previousTotal = remember(previousSpends) {
        previousSpends.fold(BigDecimal.ZERO) { sum, spend -> sum + spend.value }
    }
    val previousCategoryTotals = remember(previousSpends) {
        previousSpends
            .groupBy { it.categoryId }
            .mapValues { (_, group) ->
                group.fold(BigDecimal.ZERO) { sum, spend -> sum + spend.value }
            }
    }
    val daysElapsed = if (month == currentMonth) today.dayOfMonth else month.lengthOfMonth()
    val average = remember(total, daysElapsed) {
        if (daysElapsed <= 0) BigDecimal.ZERO
        else total.divide(BigDecimal(daysElapsed), 0, RoundingMode.HALF_UP)
    }
    val biggest = remember(monthSpends) { monthSpends.maxByOrNull { it.value } }

    val categoryTotals = remember(monthSpends, categories, total) {
        composeCategoryTotals(monthSpends, categories, total)
    }
    val dailyTotals = remember(monthSpends, month) {
        composeDailyTotals(monthSpends, month)
    }
    val peakDay = remember(dailyTotals) {
        dailyTotals.maxByOrNull { it.second }
    }
    val yearTotals = remember(spends, month) {
        (1..12).map { monthNumber ->
            val yearMonth = YearMonth.of(month.year, monthNumber)
            val total = spends
                .filter { YearMonth.from(it.date.toLocalDate()) == yearMonth }
                .fold(BigDecimal.ZERO) { sum, spend -> sum + spend.value }
            yearMonth to total
        }
    }
    val yearHasData = remember(yearTotals) { yearTotals.any { it.second > BigDecimal.ZERO } }
    val bestMonth = remember(yearTotals) {
        yearTotals.filter { it.second > BigDecimal.ZERO }.maxByOrNull { it.second }
    }
    val lowestMonth = remember(yearTotals) {
        yearTotals.filter { it.second > BigDecimal.ZERO }.minByOrNull { it.second }
    }

    val activeSubscriptions = remember(subscriptions) { subscriptions.filter { it.active } }
    val monthlyRecurring = remember(activeSubscriptions) {
        activeSubscriptions.fold(BigDecimal.ZERO) { sum, subscription ->
            sum + monthlyEquivalent(subscription)
        }
    }
    val monthlyBills = remember(activeSubscriptions, categoriesById) {
        activeSubscriptions
            .filter { subscription ->
                categoriesById[subscription.categoryId]?.name in billCategoryNames
            }
            .fold(BigDecimal.ZERO) { sum, subscription ->
                sum + monthlyEquivalent(subscription)
            }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = null,
                )
            }
            Text(
                text = prettyYearMonth(month),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            )
            IconButton(
                onClick = { month = month.minusMonths(1) },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_left),
                    contentDescription = null,
                )
            }
            IconButton(
                onClick = { month = month.plusMonths(1) },
                enabled = month < currentMonth,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
        ) {
            item("total") {
                TotalCard(
                    total = total,
                    previousTotal = previousTotal,
                    previousLabel = prettyYearMonth(previousMonth),
                    count = monthSpends.size,
                    currency = currency,
                )
            }

            if (activeSubscriptions.isNotEmpty()) {
                item("recurring") {
                    RecurringCard(
                        monthlyTotal = monthlyRecurring,
                        monthlyServices = monthlyRecurring - monthlyBills,
                        monthlyBills = monthlyBills,
                        activeCount = activeSubscriptions.size,
                        currency = currency,
                        onClick = onOpenSubscriptionInsights,
                    )
                }
            }

            if (monthSpends.isNotEmpty()) {
                item("summary") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SummaryCard(
                            label = stringResource(R.string.daily_average),
                            amount = average,
                            currency = currency,
                            container = MaterialTheme.colorScheme.secondaryContainer,
                            content = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f),
                        )
                        SummaryCard(
                            label = stringResource(R.string.max_spent),
                            amount = biggest?.value ?: BigDecimal.ZERO,
                            currency = currency,
                            container = MaterialTheme.colorScheme.surfaceContainerHigh,
                            content = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                item("categories") {
                    SectionTitle(
                        title = stringResource(R.string.by_category),
                        icon = painterResource(R.drawable.ic_label),
                    )
                    SectionCard {
                        val context = LocalContext.current
                        val segments = categoryTotals.map { categoryTotal ->
                            DonutSegment(
                                value = categoryTotal.amount.toFloat(),
                                color = categoryTotal.category
                                    ?.let { Color(it.color) }
                                    ?: MaterialTheme.colorScheme.primary,
                            )
                        }

                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        ) {
                            DonutChart(
                                segments = segments,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .size(150.dp),
                                center = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = stringResource(R.string.total_spent),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            text = numberFormat(context, total, currency),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFeatureSettings = "tnum",
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                        )
                                    }
                                },
                            )

                            Spacer(Modifier.height(16.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                categoryTotals.forEach { categoryTotal ->
                                    CategoryShareRow(
                                        categoryTotal = categoryTotal,
                                        previousAmount = previousCategoryTotals[
                                            categoryTotal.category?.id
                                        ] ?: BigDecimal.ZERO,
                                        currency = currency,
                                    )
                                }
                            }
                        }
                    }
                }

                item("days") {
                    SectionTitle(
                        title = stringResource(R.string.by_day),
                        icon = painterResource(R.drawable.ic_calendar),
                        caption = peakDay
                            ?.takeIf { it.second > BigDecimal.ZERO }
                            ?.let { peak ->
                                stringResource(
                                    R.string.peak_day,
                                    prettyDate(
                                        date = month.atDay(peak.first).toDate(),
                                        pattern = "d MMM",
                                    ),
                                )
                            },
                    )
                    SectionCard {
                        DayChart(
                            dailyTotals = dailyTotals,
                            modifier = Modifier.padding(20.dp),
                        )
                    }
                }
            } else {
                item("empty") {
                    Text(
                        text = stringResource(R.string.no_spending_month),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorOnEditor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                    )
                }
            }

            if (yearHasData) {
                item("year") {
                    SectionTitle(
                        title = stringResource(R.string.year_overview),
                        icon = painterResource(R.drawable.ic_analytics),
                    )
                    SectionCard {
                        YearOverview(
                            values = yearTotals,
                            selectedMonth = month,
                            bestMonth = bestMonth,
                            lowestMonth = lowestMonth,
                            currency = currency,
                            modifier = Modifier.padding(20.dp),
                        )
                    }
                }
            }
        }
    }
}

private data class CategoryTotal(
    val category: Category?,
    val amount: BigDecimal,
    val fraction: Float,
)

private fun composeCategoryTotals(
    spends: List<Transaction>,
    categories: List<Category>,
    total: BigDecimal,
): List<CategoryTotal> {
    if (total <= BigDecimal.ZERO) return emptyList()

    val categoriesById = categories.associateBy { it.id }

    return spends
        .groupBy { it.categoryId }
        .map { (categoryId, transactions) ->
            val amount = transactions.fold(BigDecimal.ZERO) { sum, spend -> sum + spend.value }

            CategoryTotal(
                category = categoryId?.let { categoriesById[it] },
                amount = amount,
                fraction = amount.toFloat() / total.toFloat(),
            )
        }
        .sortedByDescending { it.amount }
}

/** Day-of-month to total, with every day of the month present so the chart has a full axis. */
private fun composeDailyTotals(
    spends: List<Transaction>,
    month: YearMonth,
): List<Pair<Int, BigDecimal>> {
    val byDay = spends.groupBy { it.date.toLocalDate().dayOfMonth }

    return (1..month.lengthOfMonth()).map { day ->
        day to (byDay[day]?.fold(BigDecimal.ZERO) { sum, spend -> sum + spend.value }
            ?: BigDecimal.ZERO)
    }
}

@Composable
private fun RecurringCard(
    monthlyTotal: BigDecimal,
    monthlyServices: BigDecimal,
    monthlyBills: BigDecimal,
    activeCount: Int,
    currency: ExtendCurrency,
    onClick: () -> Unit,
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.recurring_per_month),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.active_subscriptions,
                        activeCount,
                        activeCount,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = numberFormat(context, monthlyTotal, currency),
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(
                    R.string.services_bills_split,
                    numberFormat(context, monthlyServices, currency),
                    numberFormat(context, monthlyBills, currency),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TotalCard(
    total: BigDecimal,
    previousTotal: BigDecimal,
    previousLabel: String,
    count: Int,
    currency: ExtendCurrency,
) {
    val context = LocalContext.current

    GradientCard(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                text = stringResource(R.string.total_spent),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = numberFormat(context, total, currency),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFeatureSettings = "tnum",
                ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
            val delta = percentDelta(total, previousTotal)
            if (delta != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${if (delta >= 0) "↑" else "↓"} ${abs(delta)}% " +
                            stringResource(R.string.vs_previous, previousLabel),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (delta > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }

            Spacer(Modifier.height(6.dp))
            Text(
                text = pluralStringResource(R.plurals.expenses_count, count, count),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    amount: BigDecimal,
    currency: ExtendCurrency,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.height(104.dp),
        shape = RoundedCornerShape(24.dp),
        color = container,
        contentColor = content,
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = content.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = numberFormat(context, amount, currency),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    icon: Painter? = null,
    caption: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colorOnEditor,
        )
        if (caption != null) {
            Spacer(Modifier.weight(1f))
            Text(
                text = caption,
                style = MaterialTheme.typography.labelMedium,
                color = colorOnEditor.copy(alpha = 0.6f),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SectionCard(
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(28.dp)

    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                shape = shape,
            ),
    ) {
        content()
    }
}

@Composable
private fun CategoryShareRow(
    categoryTotal: CategoryTotal,
    previousAmount: BigDecimal,
    currency: ExtendCurrency,
) {
    val context = LocalContext.current
    val color = categoryTotal.category
        ?.let { Color(it.color) }
        ?: MaterialTheme.colorScheme.primary
    val name = categoryTotal.category?.name
        ?: stringResource(R.string.no_category)
    val percent = (categoryTotal.fraction * 100f).roundToInt()
        .coerceAtLeast(if (categoryTotal.fraction > 0f) 1 else 0)

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(Modifier.width(8.dp))
            CategoryIcon(
                category = categoryTotal.category,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                color = colorOnEditor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = numberFormat(context, categoryTotal.amount, currency),
                style = MaterialTheme.typography.titleSmall,
                color = colorOnEditor,
                maxLines = 1,
                softWrap = false,
            )
        }

        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(categoryTotal.fraction.coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
            Spacer(Modifier.width(10.dp))
            val delta = percentDelta(categoryTotal.amount, previousAmount)

            Column(
                modifier = Modifier.width(52.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = stringResource(R.string.share_percent, percent),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorOnEditor.copy(alpha = 0.6f),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                )
                if (delta != null) {
                    Text(
                        text = "${if (delta >= 0) "↑" else "↓"}${abs(delta)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (delta > 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        textAlign = TextAlign.End,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun YearOverview(
    values: List<Pair<YearMonth, BigDecimal>>,
    selectedMonth: YearMonth,
    bestMonth: Pair<YearMonth, BigDecimal>?,
    lowestMonth: Pair<YearMonth, BigDecimal>?,
    currency: ExtendCurrency,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern("MMMMM", locale) }
    val max = values.maxOfOrNull { it.second } ?: BigDecimal.ZERO
    val primary = MaterialTheme.colorScheme.primary

    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            values.forEach { (yearMonth, amount) ->
                val fraction = if (max > BigDecimal.ZERO) {
                    amount.toFloat() / max.toFloat()
                } else {
                    0f
                }
                val isSelected = yearMonth == selectedMonth

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height((68.dp * fraction).coerceAtLeast(3.dp))
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    amount <= BigDecimal.ZERO -> {
                                        MaterialTheme.colorScheme.surfaceContainerHighest
                                    }

                                    isSelected -> primary

                                    else -> primary.copy(alpha = 0.42f)
                                }
                            ),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = yearMonth.format(monthFormatter),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) {
                            primary
                        } else {
                            colorOnEditor.copy(alpha = 0.55f)
                        },
                        maxLines = 1,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        val caption = buildList {
            bestMonth?.let {
                add(
                    stringResource(
                        R.string.best_month,
                        it.first.format(DateTimeFormatter.ofPattern("MMM", locale)),
                        numberFormat(context, it.second, currency),
                    )
                )
            }
            lowestMonth?.let {
                if (it.first != bestMonth?.first) {
                    add(
                        stringResource(
                            R.string.lowest_month,
                            it.first.format(DateTimeFormatter.ofPattern("MMM", locale)),
                            numberFormat(context, it.second, currency),
                        )
                    )
                }
            }
        }.joinToString(" · ")

        if (caption.isNotBlank()) {
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = colorOnEditor.copy(alpha = 0.6f),
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun DayChart(
    dailyTotals: List<Pair<Int, BigDecimal>>,
    modifier: Modifier = Modifier,
) {
    val max = dailyTotals.maxOfOrNull { it.second } ?: BigDecimal.ZERO
    val maxHeight = 104.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(maxHeight),
        contentAlignment = Alignment.BottomStart,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            dailyTotals.forEach { (_, amount) ->
                val fraction = if (max > BigDecimal.ZERO) {
                    amount.toFloat() / max.toFloat()
                } else {
                    0f
                }
                val barHeight = (maxHeight * fraction).coerceAtLeast(3.dp)

                Box(
                    Modifier
                        .weight(1f)
                        .height(barHeight)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (amount > BigDecimal.ZERO) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            }
                        ),
                )
            }
        }
    }
}
