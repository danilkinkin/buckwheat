package com.danilkinkin.buckwheat.home

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SubscriptionsViewModel
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.history.SpentItem
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.isSameDay
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.prettyYearMonth
import com.danilkinkin.buckwheat.util.toLocalDate
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.Date

private const val RECENT_COUNT = 4
private const val UPCOMING_COUNT = 3

/**
 * The home answers three questions: what did I spend today, what this month, and what is about to
 * be charged. The editor is one tap away and no longer occupies the home.
 */
@Composable
fun HomeScreen(
    onAdd: () -> Unit,
    onOpenSubscriptions: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    subscriptionsViewModel: SubscriptionsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val spends by spendsViewModel.spends.observeAsState(emptyList())
    val categories by spendsViewModel.categories.observeAsState(emptyList())
    val currency by spendsViewModel.currency.observeAsState(com.danilkinkin.buckwheat.data.ExtendCurrency.none())
    val subscriptions by subscriptionsViewModel.subscriptions.observeAsState(emptyList())

    val today = LocalDate.now()
    val currentMonth = YearMonth.from(today)
    val todayTotal = spends
        .filter { isSameDay(it.date, Date()) }
        .fold(BigDecimal.ZERO) { total, spend -> total + spend.value }
    val monthTotal = spends
        .filter { YearMonth.from(it.date.toLocalDate()) == currentMonth }
        .fold(BigDecimal.ZERO) { total, spend -> total + spend.value }

    val categoriesById = remember(categories) { categories.associateBy { it.id } }
    val upcoming = subscriptions
        .filter { it.active }
        .sortedBy { it.nextDue.time }
        .take(UPCOMING_COUNT)
    val recent = spends.takeLast(RECENT_COUNT).asReversed()

    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 104.dp),
        ) {
            item("header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = prettyYearMonth(currentMonth),
                        style = MaterialTheme.typography.labelLarge,
                        color = colorOnEditor.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
                            contentDescription = null,
                            tint = colorOnEditor,
                        )
                    }
                }
            }

            item("totals") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.today),
                            style = MaterialTheme.typography.labelMedium,
                            color = colorOnEditor.copy(alpha = 0.6f),
                        )
                        Text(
                            text = numberFormat(context, todayTotal, currency, trimDecimalPlaces = true),
                            style = MaterialTheme.typography.displaySmall,
                            color = colorOnEditor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.this_month),
                            style = MaterialTheme.typography.labelMedium,
                            color = colorOnEditor.copy(alpha = 0.6f),
                        )
                        Text(
                            text = numberFormat(context, monthTotal, currency, trimDecimalPlaces = true),
                            style = MaterialTheme.typography.titleLarge,
                            color = colorOnEditor,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }

            item("upcoming-header") {
                SectionHeader(title = stringResource(R.string.upcoming)) {
                    TextButton(onClick = onOpenSubscriptions) {
                        Text(stringResource(R.string.see_all))
                    }
                }
            }

            if (upcoming.isEmpty()) {
                item("upcoming-empty") {
                    Text(
                        text = stringResource(R.string.no_subscriptions),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorOnEditor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    )
                }
            } else {
                items(upcoming, key = { "sub-${it.id}" }) { subscription ->
                    UpcomingSubscriptionRow(
                        subscription = subscription,
                        category = subscription.categoryId?.let { categoriesById[it] },
                        currency = currency,
                        onClick = onOpenSubscriptions,
                    )
                }
            }

            item("recent-header") {
                SectionHeader(title = stringResource(R.string.recent)) {
                    TextButton(onClick = onOpenHistory) {
                        Text(stringResource(R.string.see_all))
                    }
                }
            }

            if (recent.isEmpty()) {
                item("recent-empty") {
                    Text(
                        text = stringResource(R.string.no_spends),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorOnEditor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    )
                }
            } else {
                items(recent, key = { "spent-${it.uid}" }) { spend ->
                    SpentItem(
                        transaction = spend,
                        currency = currency,
                        category = spend.categoryId?.let { categoriesById[it] },
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.add_expense),
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 8.dp, top = 20.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = colorOnEditor.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f),
        )
        action()
    }
}

@Composable
private fun UpcomingSubscriptionRow(
    subscription: Subscription,
    category: Category?,
    currency: com.danilkinkin.buckwheat.data.ExtendCurrency,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val isDue = subscription.nextDue.time <= System.currentTimeMillis()
    val dueLabel = prettyDate(subscription.nextDue, pattern = "d MMM", simplifyIfToday = true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (!category?.emoji.isNullOrEmpty()) {
                    Text(category.emoji, style = MaterialTheme.typography.titleSmall)
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_balance_wallet),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

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
                text = dueLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (isDue) {
                    MaterialTheme.colorScheme.error
                } else {
                    colorOnEditor.copy(alpha = 0.6f)
                },
                maxLines = 1,
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = numberFormat(context, subscription.amount, currency),
            style = MaterialTheme.typography.titleMedium,
            color = colorOnEditor,
            maxLines = 1,
        )
    }
}
