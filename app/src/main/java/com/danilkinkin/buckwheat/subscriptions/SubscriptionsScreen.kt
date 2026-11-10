package com.danilkinkin.buckwheat.subscriptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.draw.clip
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ListCard
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.ServiceCatalog
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SubscriptionsViewModel
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.SubscriptionService
import com.danilkinkin.buckwheat.data.entities.toSubscriptionPeriod
import com.danilkinkin.buckwheat.history.SwipeActions
import com.danilkinkin.buckwheat.history.SwipeActionsConfig
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toLocalDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SubscriptionsScreen(
    onBack: () -> Unit = {},
    onAddSubscription: () -> Unit = {},
    onEditSubscription: (Int) -> Unit = {},
    onOpenInsights: () -> Unit = {},
    subscriptionsViewModel: SubscriptionsViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val subscriptions by subscriptionsViewModel.subscriptions.observeAsState(emptyList())
    val categories by spendsViewModel.categories.observeAsState(emptyList())
    val currency by spendsViewModel.currency.observeAsState(
        com.danilkinkin.buckwheat.data.ExtendCurrency.none()
    )

    val categoriesById = remember(categories) { categories.associateBy { it.id } }
    val removedMessage = stringResource(R.string.remove_subscription)
    val undoLabel = stringResource(R.string.remove_spent_undo)

    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
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
                    text = stringResource(R.string.subscriptions_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onOpenInsights) {
                    Icon(
                        painter = painterResource(R.drawable.ic_analytics),
                        contentDescription = stringResource(R.string.subscription_insights),
                    )
                }
            }

            if (subscriptions.isEmpty()) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.no_subscriptions),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(subscriptions, key = { it.id }) { subscription ->
                        val service = ServiceCatalog.find(subscription.serviceId)
                        val category = subscription.categoryId?.let { categoriesById[it] }

                        SwipeActions(
                            modifier = Modifier.clip(RoundedCornerShape(24.dp)),
                            startActionsConfig = SwipeActionsConfig(
                                threshold = 0.4f,
                                background = MaterialTheme.colorScheme.tertiaryContainer,
                                backgroundActive = MaterialTheme.colorScheme.tertiary,
                                iconTint = MaterialTheme.colorScheme.onTertiary,
                                icon = painterResource(R.drawable.ic_edit),
                                stayDismissed = false,
                                onDismiss = { onEditSubscription(subscription.id) },
                            ),
                            endActionsConfig = SwipeActionsConfig(
                                threshold = 0.4f,
                                background = MaterialTheme.colorScheme.errorContainer,
                                backgroundActive = MaterialTheme.colorScheme.error,
                                iconTint = MaterialTheme.colorScheme.onError,
                                icon = painterResource(R.drawable.ic_delete_forever),
                                stayDismissed = true,
                                onDismiss = {
                                    subscriptionsViewModel.delete(subscription.id)
                                    appViewModel.showSnackbar(
                                        message = removedMessage,
                                        actionLabel = undoLabel,
                                        duration = SnackbarDuration.Long,
                                    ) { result ->
                                        if (result == SnackbarResult.ActionPerformed) {
                                            subscriptionsViewModel.restore(subscription)
                                        }
                                    }
                                },
                            ),
                        ) {
                            ListCard(
                                accent = service?.let { Color(it.brandColor) }
                                    ?: category?.let { Color(it.color) },
                            ) {
                                SubscriptionRow(
                                    subscription = subscription,
                                    service = service,
                                    category = category,
                                    currency = currency,
                                    onClick = { onEditSubscription(subscription.id) },
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddSubscription,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = stringResource(R.string.add_subscription),
            )
        }
    }


}

@Composable
private fun SubscriptionRow(
    subscription: Subscription,
    service: SubscriptionService?,
    category: Category?,
    currency: com.danilkinkin.buckwheat.data.ExtendCurrency,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val isDue = subscription.nextDue.time <= System.currentTimeMillis()
    val periodLabel = subscriptionIntervalLabel(
        count = subscription.intervalCount,
        period = subscription.period.toSubscriptionPeriod(),
    )
    val dueLabel = prettyDate(subscription.nextDue, pattern = "d MMM", simplifyIfToday = true)
    val daysUntilDue = ChronoUnit.DAYS.between(
        LocalDate.now(),
        subscription.nextDue.toLocalDate(),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (service != null) {
            ServiceBadge(service = service, size = 48.dp)
        } else if (!category?.emoji.isNullOrEmpty()) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color(category.color).copy(alpha = 0.22f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = category.emoji,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        } else {
            ServiceBadge(service = null, size = 48.dp)
        }
        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$periodLabel · $dueLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDue) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(6.dp))
                DueStatusChip(daysUntilDue = daysUntilDue)
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = numberFormat(context, subscription.amount, currency),
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
            maxLines = 1,
        )
    }
}

