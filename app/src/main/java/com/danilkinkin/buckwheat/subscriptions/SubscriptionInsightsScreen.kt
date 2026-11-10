package com.danilkinkin.buckwheat.subscriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.ServiceCatalog
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SubscriptionsViewModel
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.entities.toSubscriptionPeriod
import com.danilkinkin.buckwheat.data.monthlyEquivalent
import com.danilkinkin.buckwheat.data.nextSubscriptionDate
import com.danilkinkin.buckwheat.data.SubscriptionService
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toLocalDate
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun SubscriptionInsightsScreen(
    onBack: () -> Unit,
    subscriptionsViewModel: SubscriptionsViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
) {
    val subscriptions by subscriptionsViewModel.subscriptions.observeAsState(emptyList())
    val spends by spendsViewModel.spends.observeAsState(emptyList())
    val categories by spendsViewModel.categories.observeAsState(emptyList())
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())

    val active = remember(subscriptions) { subscriptions.filter { it.active } }
    val monthlyBySubscription = remember(active) {
        active.associateWith { monthlyEquivalent(it) }
    }
    val monthlyTotal = remember(monthlyBySubscription) {
        monthlyBySubscription.values.fold(BigDecimal.ZERO) { sum, value -> sum + value }
    }
    val paidThisMonth = remember(spends) {
        val month = YearMonth.now()
        spends
            .filter { it.serviceId != null && YearMonth.from(it.date.toLocalDate()) == month }
            .fold(BigDecimal.ZERO) { sum, spend -> sum + spend.value }
    }
    val dueIn30Days = remember(active) {
        active.fold(BigDecimal.ZERO) { sum, subscription ->
            sum + amountDueWithin(subscription, 30)
        }
    }
    val renewals = remember(active) { active.sortedBy { it.nextDue.time }.take(5) }
    val topServices = remember(active, monthlyBySubscription) {
        active
            .map { it to (monthlyBySubscription[it] ?: BigDecimal.ZERO) }
            .sortedByDescending { it.second }
            .take(6)
    }
    val fallbackCategoryColor = MaterialTheme.colorScheme.primary
    val categoryShares = remember(
        active,
        categories,
        monthlyBySubscription,
        fallbackCategoryColor,
    ) {
        val byId = categories.associateBy { it.id }
        active
            .groupBy { it.categoryId }
            .map { (categoryId, group) ->
                val amount = group.fold(BigDecimal.ZERO) { sum, subscription ->
                    sum + (monthlyBySubscription[subscription] ?: BigDecimal.ZERO)
                }
                val category = categoryId?.let { byId[it] }
                CategoryShare(
                    name = category?.name,
                    emoji = category?.emoji,
                    color = category?.let { Color(it.color) } ?: fallbackCategoryColor,
                    amount = amount,
                )
            }
            .sortedByDescending { it.amount }
    }
    val lastSixMonths = remember(spends) {
        val current = YearMonth.now()
        (5 downTo 0).map { offset ->
            val month = current.minusMonths(offset.toLong())
            val total = spends
                .filter {
                    it.serviceId != null &&
                            YearMonth.from(it.date.toLocalDate()) == month
                }
                .fold(BigDecimal.ZERO) { sum, spend -> sum + spend.value }
            month to total
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
                text = stringResource(R.string.subscription_insights),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (active.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.no_subscriptions),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorOnEditor.copy(alpha = 0.7f),
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
        ) {
            item("hero") {
                RecurringHero(
                    monthlyTotal = monthlyTotal,
                    annualTotal = monthlyTotal.multiply(BigDecimal(12)),
                    activeCount = active.size,
                    dueIn30Days = dueIn30Days,
                    currency = currency,
                )
            }

            item("comparison") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InsightStatCard(
                        label = stringResource(R.string.projected_month),
                        amount = monthlyTotal,
                        currency = currency,
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        content = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    InsightStatCard(
                        label = stringResource(R.string.paid_this_month),
                        amount = paidThisMonth,
                        currency = currency,
                        container = MaterialTheme.colorScheme.surfaceContainerHigh,
                        content = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item("top") {
                InsightSectionTitle(stringResource(R.string.top_services_month))
                InsightCard {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        val max = topServices.maxOfOrNull { it.second } ?: BigDecimal.ZERO
                        topServices.forEach { (subscription, amount) ->
                            TopServiceRow(
                                subscription = subscription,
                                service = ServiceCatalog.find(subscription.serviceId),
                                amount = amount,
                                max = max,
                                currency = currency,
                            )
                        }
                    }
                }
            }

            item("renewals") {
                InsightSectionTitle(stringResource(R.string.upcoming_renewals))
                InsightCard {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        renewals.forEach { subscription ->
                            RenewalRow(
                                subscription = subscription,
                                service = ServiceCatalog.find(subscription.serviceId),
                                currency = currency,
                            )
                        }
                    }
                }
            }

            item("categories") {
                InsightSectionTitle(stringResource(R.string.by_category))
                InsightCard {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        categoryShares.forEach { share ->
                            CategoryShareRow(
                                share = share,
                                total = monthlyTotal,
                                currency = currency,
                            )
                        }
                    }
                }
            }

            item("trend") {
                InsightSectionTitle(stringResource(R.string.actual_subscription_spend))
                InsightCard {
                    TrendBars(
                        values = lastSixMonths,
                        currency = currency,
                        modifier = Modifier.padding(20.dp),
                    )
                }
            }
        }
    }
}

private data class CategoryShare(
    val name: String?,
    val emoji: String?,
    val color: Color,
    val amount: BigDecimal,
)

@Composable
private fun RecurringHero(
    monthlyTotal: BigDecimal,
    annualTotal: BigDecimal,
    activeCount: Int,
    dueIn30Days: BigDecimal,
    currency: ExtendCurrency,
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.recurring_per_month),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = numberFormat(context, monthlyTotal, currency),
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                softWrap = false,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${numberFormat(context, annualTotal, currency)} / year · " +
                        pluralStringResource(
                            R.plurals.active_subscriptions,
                            activeCount,
                            activeCount,
                        ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(
                    R.string.due_next_30_days,
                    numberFormat(context, dueIn30Days, currency),
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun InsightStatCard(
    label: String,
    amount: BigDecimal,
    currency: ExtendCurrency,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.height(96.dp),
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
private fun InsightSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = colorOnEditor,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
    )
}

@Composable
private fun InsightCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        content()
    }
}

@Composable
private fun TopServiceRow(
    subscription: Subscription,
    service: SubscriptionService?,
    amount: BigDecimal,
    max: BigDecimal,
    currency: ExtendCurrency,
) {
    val context = LocalContext.current
    val fraction = if (max > BigDecimal.ZERO) amount.toFloat() / max.toFloat() else 0f

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ServiceBadge(service = service, size = 36.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.bodyLarge,
                color = colorOnEditor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = numberFormat(context, amount, currency),
                style = MaterialTheme.typography.titleSmall,
                color = colorOnEditor,
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        service?.let { Color(it.brandColor) }
                            ?: MaterialTheme.colorScheme.primary
                    ),
            )
        }
    }
}

@Composable
private fun RenewalRow(
    subscription: Subscription,
    service: SubscriptionService?,
    currency: ExtendCurrency,
) {
    val context = LocalContext.current
    val days = java.time.temporal.ChronoUnit.DAYS.between(
        LocalDate.now(),
        subscription.nextDue.toLocalDate(),
    )
    val dueSoon = days <= 7

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ServiceBadge(service = service, size = 36.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.bodyLarge,
                color = colorOnEditor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = prettyDate(
                    subscription.nextDue,
                    pattern = "d MMM",
                    simplifyIfToday = true,
                ),
                style = MaterialTheme.typography.labelSmall,
                color = if (dueSoon) {
                    MaterialTheme.colorScheme.error
                } else {
                    colorOnEditor.copy(alpha = 0.6f)
                },
            )
        }
        Text(
            text = numberFormat(context, subscription.amount, currency),
            style = MaterialTheme.typography.titleSmall,
            color = colorOnEditor,
        )
    }
}

@Composable
private fun CategoryShareRow(
    share: CategoryShare,
    total: BigDecimal,
    currency: ExtendCurrency,
) {
    val context = LocalContext.current
    val fraction = if (total > BigDecimal.ZERO) {
        share.amount.toFloat() / total.toFloat()
    } else {
        0f
    }
    val percent = (fraction * 100).roundToInt().coerceAtLeast(if (fraction > 0f) 1 else 0)

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(share.color),
            )
            Spacer(Modifier.width(8.dp))
            if (!share.emoji.isNullOrEmpty()) {
                Text(share.emoji, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = share.name ?: stringResource(R.string.no_category),
                style = MaterialTheme.typography.bodyLarge,
                color = colorOnEditor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = numberFormat(context, share.amount, currency),
                style = MaterialTheme.typography.titleSmall,
                color = colorOnEditor,
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
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(share.color),
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelSmall,
                color = colorOnEditor.copy(alpha = 0.6f),
                modifier = Modifier.width(40.dp),
            )
        }
    }
}

@Composable
private fun TrendBars(
    values: List<Pair<YearMonth, BigDecimal>>,
    currency: ExtendCurrency,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val max = values.maxOfOrNull { it.second } ?: BigDecimal.ZERO
    val formatter = remember(locale) { DateTimeFormatter.ofPattern("MMM", locale) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(124.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        values.forEach { (month, amount) ->
            val fraction = if (max > BigDecimal.ZERO) {
                amount.toFloat() / max.toFloat()
            } else {
                0f
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height((78.dp * fraction).coerceAtLeast(3.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (amount > BigDecimal.ZERO) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest
                            }
                        ),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = month.format(formatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorOnEditor.copy(alpha = 0.6f),
                    maxLines = 1,
                )
                Text(
                    text = numberFormat(context, amount, currency),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorOnEditor.copy(alpha = 0.45f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Sum of the occurrences that fall inside the next [days] days, including overdue ones. */
private fun amountDueWithin(subscription: Subscription, days: Long): BigDecimal {
    val end = LocalDate.now().plusDays(days)
    var cursor = subscription.nextDue.toLocalDate()
    var total = BigDecimal.ZERO
    var guard = 0

    while (!cursor.isAfter(end) && guard < 60) {
        total += subscription.amount
        cursor = nextSubscriptionDate(
            period = subscription.period.toSubscriptionPeriod(),
            intervalCount = subscription.intervalCount,
            anchorDay = subscription.anchorDay,
            from = cursor,
        )
        guard++
    }

    return total
}
