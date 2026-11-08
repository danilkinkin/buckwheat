package com.danilkinkin.buckwheat.subscriptions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CategoryChip
import com.danilkinkin.buckwheat.base.CategoryDropdown
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.entities.SubscriptionPeriod
import com.danilkinkin.buckwheat.data.entities.toSubscriptionPeriod
import com.danilkinkin.buckwheat.editor.dateTimeEdit.DatePickerDialog
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubscriptionForm(
    initial: Subscription?,
    categories: List<Category>,
    onSave: (name: String, amount: BigDecimal, period: SubscriptionPeriod, nextDue: Date, categoryId: Int?) -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null,
    onAddCategory: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var amountText by remember { mutableStateOf(initial?.amount?.toPlainString() ?: "") }
    var period by remember {
        mutableStateOf(initial?.period?.toSubscriptionPeriod() ?: SubscriptionPeriod.MONTHLY)
    }
    var nextDue by remember {
        mutableStateOf(initial?.nextDue ?: LocalDate.now().plusMonths(1).toDate())
    }
    var categoryId by remember { mutableStateOf(initial?.categoryId) }
    var categoryTouched by remember { mutableStateOf(initial != null) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(categories) {
        if (initial == null && !categoryTouched && categoryId == null && categories.isNotEmpty()) {
            categoryId = categories.firstOrNull { it.name.equals("Other", ignoreCase = true) }?.id
        }
    }

    val amount = amountText.toBigDecimalOrNull()

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
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
        CategoryDropdown(
            categories = categories,
            selectedCategoryId = categoryId,
            onSelect = {
                categoryTouched = true
                categoryId = it
            },
            onAddCategory = onAddCategory,
        )

        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            if (onCancel != null) {
                TextButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.textButtonColors(),
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Spacer(Modifier.width(8.dp))
            }
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

@Composable
private fun SubscriptionPeriod.label(): String = when (this) {
    SubscriptionPeriod.WEEKLY -> stringResource(R.string.period_weekly)
    SubscriptionPeriod.MONTHLY -> stringResource(R.string.period_monthly)
    SubscriptionPeriod.YEARLY -> stringResource(R.string.period_yearly)
}
