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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.danilkinkin.buckwheat.history.SpentItem
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.isSameDay
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toLocalDate
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Date

private const val RECENT_COUNT = 4
private const val UPCOMING_COUNT = 3

@Composable
fun HomeScreen(
    onAdd: () -> Unit,
    onOpenSubscriptions: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenSettings: () -> Unit,
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    subscriptionsViewModel: SubscriptionsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val spends by spendsViewModel.spends.observeAsState(emptyList())
    val categories by spendsViewModel.categories.observeAsState(emptyList())
    val currency by spendsViewModel.currency.observeAsState(
        com.danilkinkin.buckwheat.data.ExtendCurrency.none()
    )
    val subscriptions by subscriptionsViewModel.subscriptions.observeAsState(emptyList())

    val currentMonth = YearMonth.from(LocalDate.now())
    val todayDate = LocalDate.now()
    val locale = LocalConfiguration.current.locales[0]
    val dateText = if (locale.language == "en") {
        val weekday = todayDate.format(DateTimeFormatter.ofPattern("EEE", locale))
        val month = todayDate.format(DateTimeFormatter.ofPattern("MMM", locale))
        "$weekday., ${ordinal(todayDate.dayOfMonth)} $month. ${todayDate.year}"
    } else {
        todayDate.format(DateTimeFormatter.ofPattern("EEE., d MMM yyyy", locale))
    }
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
            contentPadding = PaddingValues(top = 8.dp, bottom = 104.dp),
        ) {
            item("header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.titleLarge,
                        color = colorOnEditor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onOpenStats) {
                        Icon(
                            painter = painterResource(R.drawable.ic_analytics),
                            contentDescription = stringResource(R.string.stats_title),
                            tint = colorOnEditor,
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings),
                            contentDescription = null,
                            tint = colorOnEditor,
                        )
                    }
                }
            }

            item("heroes") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HeroCard(
                        label = stringResource(R.string.today),
                        amount = todayTotal,
                        currency = currency,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        content = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    HeroCard(
                        label = stringResource(R.string.this_month),
                        amount = monthTotal,
                        currency = currency,
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        content = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item("upcoming") {
                SectionTitle(
                    title = stringResource(R.string.upcoming),
                    count = upcoming.size,
                    action = onOpenSubscriptions,
                )
                SectionCard {
                    if (upcoming.isEmpty()) {
                        SectionEmpty(stringResource(R.string.no_subscriptions))
                    } else {
                        upcoming.forEach { subscription ->
                            UpcomingSubscriptionRow(
                                subscription = subscription,
                                category = subscription.categoryId?.let { categoriesById[it] },
                                currency = currency,
                                onClick = onOpenSubscriptions,
                            )
                        }
                    }
                }
            }

            item("recent") {
                SectionTitle(
                    title = stringResource(R.string.recent),
                    count = recent.size,
                    action = onOpenHistory,
                )
                SectionCard {
                    if (recent.isEmpty()) {
                        SectionEmpty(stringResource(R.string.no_spends))
                    } else {
                        recent.forEach { spend ->
                            SpentItem(
                                transaction = spend,
                                currency = currency,
                                category = spend.categoryId?.let { categoriesById[it] },
                            )
                        }
                    }
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
private fun HeroCard(
    label: String,
    amount: BigDecimal,
    currency: com.danilkinkin.buckwheat.data.ExtendCurrency,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.height(112.dp),
        shape = RoundedCornerShape(28.dp),
        color = container,
        contentColor = content,
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = content.copy(alpha = 0.7f),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = numberFormat(context, amount, currency),
                style = MaterialTheme.typography.headlineMedium,
                color = content,
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
    count: Int,
    action: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colorOnEditor,
        )
        if (count > 0) {
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        Spacer(Modifier.weight(1f))
        TextButton(onClick = action) {
            Text(stringResource(R.string.see_all))
        }
    }
}

@Composable
private fun SectionCard(
    content: @Composable () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SectionEmpty(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = colorOnEditor.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
    )
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!category?.emoji.isNullOrEmpty()) {
            Text(
                text = category.emoji,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.width(32.dp),
            )
        } else {
            Spacer(Modifier.width(32.dp))
        }

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

private fun ordinal(day: Int): String {
    val suffix = when {
        day % 100 in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }

    return "$day$suffix"
}
