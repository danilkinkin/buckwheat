package com.danilkinkin.buckwheat.subscriptions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.CategoryChip
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SubscriptionsViewModel
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.entities.SubscriptionPeriod
import com.danilkinkin.buckwheat.data.entities.toSubscriptionPeriod
import com.danilkinkin.buckwheat.editor.dateTimeEdit.DatePickerDialog
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Date

@Composable
fun SubscriptionsScreen(
    subscriptionsViewModel: SubscriptionsViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val subscriptions by subscriptionsViewModel.subscriptions.observeAsState(emptyList())
    val categories by spendsViewModel.categories.observeAsState(emptyList())
    val currency by spendsViewModel.currency.observeAsState(
        com.danilkinkin.buckwheat.data.ExtendCurrency.none()
    )

    var editing by remember { mutableStateOf<Subscription?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val categoriesById = remember(categories) { categories.associateBy { it.id } }

    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
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
                            onClick = {
                                editing = subscription
                                showDialog = true
                            },
                            onPaid = { subscriptionsViewModel.markPaid(subscription) },
                            onDelete = { subscriptionsViewModel.delete(subscription.id) },
                        )
                    }
                }
            }

            ButtonRow(
                icon = painterResource(R.drawable.ic_balance_wallet),
                text = stringResource(R.string.add_subscription),
                onClick = {
                    editing = null
                    showDialog = true
                },
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDialog) {
        AddSubscriptionDialog(
            initial = editing,
            categories = categories,
            onSave = { name, amount, period, nextDue, categoryId ->
                if (editing == null) {
                    subscriptionsViewModel.add(name, amount, period, nextDue, categoryId)
                } else {
                    subscriptionsViewModel.update(
                        editing!!.copy(
                            name = name.trim(),
                            amount = amount,
                            period = period.name,
                            anchorDay = nextDue.toLocalDate().dayOfMonth,
                            nextDue = nextDue,
                            categoryId = categoryId,
                        )
                    )
                }
                showDialog = false
            },
            onClose = { showDialog = false },
        )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddSubscriptionDialog(
    initial: Subscription?,
    categories: List<Category>,
    onSave: (name: String, amount: BigDecimal, period: SubscriptionPeriod, nextDue: Date, categoryId: Int?) -> Unit,
    onClose: () -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var amountText by remember { mutableStateOf(initial?.amount?.toPlainString() ?: "") }
    var period by remember {
        mutableStateOf(initial?.period?.toSubscriptionPeriod() ?: SubscriptionPeriod.MONTHLY)
    }
    var nextDue by remember {
        mutableStateOf(initial?.nextDue ?: LocalDate.now().plusMonths(1).toDate())
    }
    var categoryId by remember { mutableStateOf(initial?.categoryId) }
    var showDatePicker by remember { mutableStateOf(false) }

    val amount = amountText.toBigDecimalOrNull()

    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            ) {
                Text(
                    text = stringResource(
                        if (initial == null) R.string.add_subscription else R.string.edit_subscription
                    ),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.subscription_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value ->
                        amountText = value.filter { it.isDigit() || it == '.' || it == ',' }
                            .replace(',', '.')
                    },
                    label = { Text(stringResource(R.string.amount)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.period),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SubscriptionPeriod.entries.forEach { option ->
                        CategoryChip(
                            text = option.label(),
                            selected = option == period,
                            onClick = { period = option },
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.next_due),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(6.dp))
                CategoryChip(
                    text = prettyDate(nextDue, pattern = "d MMM yyyy", simplifyIfToday = true),
                    selected = false,
                    onClick = { showDatePicker = true },
                )
                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.category),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CategoryChip(
                        text = stringResource(R.string.no_category),
                        selected = categoryId == null,
                        onClick = { categoryId = null },
                    )
                    categories.forEach { category ->
                        CategoryChip(
                            text = category.name,
                            emoji = category.emoji,
                            selected = category.id == categoryId,
                            onClick = {
                                categoryId = if (categoryId == category.id) null else category.id
                            },
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onClose,
                        colors = ButtonDefaults.textButtonColors(),
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val value = amount ?: return@Button
                            onSave(name, value, period, nextDue, categoryId)
                        },
                        enabled = name.isNotBlank() && amount != null && amount > BigDecimal.ZERO,
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            initDate = nextDue.toLocalDate(),
            disableBeforeDate = null,
            disableAfterDate = null,
            onSelect = { date ->
                nextDue = date.toDate()
                showDatePicker = false
            },
            onClose = { showDatePicker = false },
        )
    }
}
