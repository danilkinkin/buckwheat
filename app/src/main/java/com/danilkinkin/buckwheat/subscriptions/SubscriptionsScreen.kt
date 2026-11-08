package com.danilkinkin.buckwheat.subscriptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SubscriptionsViewModel
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.entities.SubscriptionPeriod
import com.danilkinkin.buckwheat.data.entities.toSubscriptionPeriod
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toLocalDate

@Composable
fun SubscriptionsScreen(
    onBack: () -> Unit = {},
    onAddSubscription: () -> Unit = {},
    subscriptionsViewModel: SubscriptionsViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
) {
    val subscriptions by subscriptionsViewModel.subscriptions.observeAsState(emptyList())
    val categories by spendsViewModel.categories.observeAsState(emptyList())
    val currency by spendsViewModel.currency.observeAsState(
        com.danilkinkin.buckwheat.data.ExtendCurrency.none()
    )

    var editing by remember { mutableStateOf<Subscription?>(null) }
    val categoriesById = remember(categories) { categories.associateBy { it.id } }

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    items(subscriptions, key = { it.id }) { subscription ->
                        SubscriptionRow(
                            subscription = subscription,
                            category = subscription.categoryId?.let { categoriesById[it] },
                            currency = currency,
                            onClick = { editing = subscription },
                            onPaid = { subscriptionsViewModel.markPaid(subscription) },
                            onDelete = { subscriptionsViewModel.delete(subscription.id) },
                        )
                    }
                }
            }

            ButtonRow(
                icon = painterResource(R.drawable.ic_balance_wallet),
                text = stringResource(R.string.add_subscription),
                onClick = onAddSubscription,
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    val subscriptionBeingEdited = editing
    if (subscriptionBeingEdited != null) {
        Dialog(onDismissRequest = { editing = null }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.edit_subscription),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.height(16.dp))
                    SubscriptionForm(
                        initial = subscriptionBeingEdited,
                        categories = categories,
                        onSave = { name, amount, period, nextDue, categoryId ->
                            subscriptionsViewModel.update(
                                subscriptionBeingEdited.copy(
                                    name = name.trim(),
                                    amount = amount,
                                    period = period.name,
                                    anchorDay = nextDue.toLocalDate().dayOfMonth,
                                    nextDue = nextDue,
                                    categoryId = categoryId,
                                )
                            )
                            editing = null
                        },
                        onCancel = { editing = null },
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriptionRow(
    subscription: Subscription,
    category: Category?,
    currency: com.danilkinkin.buckwheat.data.ExtendCurrency,
    onClick: () -> Unit,
    onPaid: () -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    val isDue = subscription.nextDue.time <= System.currentTimeMillis()
    val periodLabel = subscription.period.toSubscriptionPeriod().label()
    val dueLabel = prettyDate(subscription.nextDue, pattern = "d MMM", simplifyIfToday = true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 24.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = listOfNotNull(
                    category?.emoji?.takeIf { it.isNotEmpty() },
                    subscription.name,
                ).joinToString(" "),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$periodLabel · $dueLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = numberFormat(context, subscription.amount, currency),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )
            if (isDue) {
                TextButton(
                    onClick = onPaid,
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                ) {
                    Text(
                        text = stringResource(R.string.mark_as_paid),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        IconButton(onClick = onDelete) {
            Icon(
                painter = painterResource(R.drawable.ic_delete_forever),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun SubscriptionPeriod.label(): String = when (this) {
    SubscriptionPeriod.WEEKLY -> stringResource(R.string.period_weekly)
    SubscriptionPeriod.MONTHLY -> stringResource(R.string.period_monthly)
    SubscriptionPeriod.YEARLY -> stringResource(R.string.period_yearly)
}
