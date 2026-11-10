package com.danilkinkin.buckwheat.home

import androidx.compose.foundation.background
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
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.ServiceCatalog
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SubscriptionsViewModel
import com.danilkinkin.buckwheat.data.SubscriptionService
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.base.CategoryIcon
import com.danilkinkin.buckwheat.base.GradientCard
import com.danilkinkin.buckwheat.base.ListCard
import com.danilkinkin.buckwheat.history.SpentItem
import com.danilkinkin.buckwheat.subscriptions.ServiceBadge
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
    onEditExpense: (Transaction) -> Unit,
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
        // A soft accent glow behind the fixed header and heroes.
        Box(
            Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                    )
                ),
        )

        Column(Modifier.fillMaxSize()) {
            // The date and the two totals stay put; only the sections below scroll.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 8.dp, top = 4.dp),
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
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer,
                    ),
                    modifier = Modifier.weight(1f),
                )
                HeroCard(
                    label = stringResource(R.string.this_month),
                    amount = monthTotal,
                    currency = currency,
                    colors = listOf(
                        MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.colorScheme.primaryContainer,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 104.dp),
            ) {
                item("upcoming") {
                    SectionTitle(
                        title = stringResource(R.string.upcoming),
                        count = upcoming.size,
                        icon = painterResource(R.drawable.ic_calendar),
                        action = onOpenSubscriptions,
                    )

                    if (upcoming.isEmpty()) {
                        ListCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                            SectionEmpty(stringResource(R.string.no_subscriptions))
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ) {
                            Column {
                                upcoming.forEach { subscription ->
                                    val service = ServiceCatalog.find(subscription.serviceId)
                                    val category = subscription.categoryId?.let { categoriesById[it] }

                                    UpcomingSubscriptionRow(
                                        subscription = subscription,
                                        service = service,
                                        category = category,
                                        currency = currency,
                                        onClick = onOpenSubscriptions,
                                    )
                                }
                            }
                        }
                    }
                }

                item("recent") {
                    SectionTitle(
                        title = stringResource(R.string.recent),
                        count = recent.size,
                        icon = painterResource(R.drawable.ic_clock),
                        action = onOpenHistory,
                    )
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (recent.isEmpty()) {
                            ListCard {
                                SectionEmpty(stringResource(R.string.no_spends))
                            }
                        } else {
                            recent.forEach { spend ->
                                val service = ServiceCatalog.find(spend.serviceId)
                                val category = spend.categoryId?.let { categoriesById[it] }

                                ListCard(
                                    accent = service?.let { Color(it.brandColor) }
                                        ?: category?.let { Color(it.color) },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .clickable { onEditExpense(spend) },
                                ) {
                                    SpentItem(
                                        transaction = spend,
                                        currency = currency,
                                        category = category,
                                        service = service,
                                        showDate = true,
                                    )
                                }
                            }
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
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
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
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    GradientCard(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(28.dp),
        colors = colors,
    ) {
        val contentColor = LocalContentColor.current

        Column(Modifier.padding(18.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.75f),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = numberFormat(context, amount, currency),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFeatureSettings = "tnum",
                ),
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
    icon: Painter? = null,
    action: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 20.dp, bottom = 8.dp),
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
        if (count > 0) {
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
    service: SubscriptionService?,
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (service != null) {
            ServiceBadge(service = service, size = 48.dp)
        } else if (category != null) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(category.color).copy(alpha = 0.22f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CategoryIcon(
                        category = category,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        } else {
            Spacer(Modifier.width(48.dp))
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
            style = MaterialTheme.typography.titleMedium.copy(
                fontFeatureSettings = "tnum",
            ),
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
