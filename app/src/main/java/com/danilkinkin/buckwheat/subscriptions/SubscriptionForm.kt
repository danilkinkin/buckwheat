package com.danilkinkin.buckwheat.subscriptions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CategoryPicker
import com.danilkinkin.buckwheat.base.MinimalTextField
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.entities.SubscriptionPeriod
import com.danilkinkin.buckwheat.data.entities.toSubscriptionPeriod
import com.danilkinkin.buckwheat.editor.dateTimeEdit.DatePickerDialog
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import kotlinx.coroutines.delay
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionForm(
    initial: Subscription?,
    categories: List<Category>,
    onSave: (name: String, amount: BigDecimal, period: SubscriptionPeriod, nextDue: Date, categoryId: Int?) -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null,
    onAddCategory: (() -> Unit)? = null,
    autoFocusName: Boolean = false,
    /**
     * Only used when the form appears together with the sheet opening. A non-zero delay lets the
     * entrance animation finish first. Tab switches must pass 0 so the keyboard is handed from one
     * field straight to the other instead of closing and re-opening.
     */
    autoFocusDelayMillis: Long = 0L,
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

    val nameFocusRequester = remember { FocusRequester() }
    val amount = amountText.toBigDecimalOrNull()

    // Immediate focus uses DisposableEffect (like the amount editor) so the hand-off happens in
    // the same frame the old field is disposed and the IME stays up. The delayed path is only for
    // the sheet's first entrance.
    if (autoFocusName) {
        if (autoFocusDelayMillis > 0L) {
            LaunchedEffect(Unit) {
                delay(autoFocusDelayMillis)
                nameFocusRequester.requestFocus()
            }
        } else {
            DisposableEffect(Unit) {
                nameFocusRequester.requestFocus()
                onDispose {}
            }
        }
    }

    LaunchedEffect(categories) {
        if (initial == null && !categoryTouched && categoryId == null && categories.isNotEmpty()) {
            categoryId = categories.firstOrNull { it.name.equals("Other", ignoreCase = true) }?.id
        }
    }

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
        MinimalTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = stringResource(R.string.subscription_name),
            textStyle = MaterialTheme.typography.titleMedium,
            modifier = Modifier.focusRequester(nameFocusRequester),
        )

        Spacer(Modifier.height(10.dp))

        MinimalTextField(
            value = amountText,
            onValueChange = { value ->
                amountText = value.filter { it.isDigit() || it == '.' || it == ',' }
                    .replace(',', '.')
            },
            placeholder = stringResource(R.string.amount),
            textStyle = MaterialTheme.typography.displaySmall,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )

        Spacer(Modifier.height(16.dp))

        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SubscriptionPeriod.entries.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = option == period,
                    onClick = { period = option },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = SubscriptionPeriod.entries.size,
                    ),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    label = {
                        Text(
                            text = option.label(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .clickable { showDatePicker = true },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f),
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar),
                    contentDescription = null,
                    modifier = Modifier.width(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.next_due),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = prettyDate(nextDue, pattern = "d MMM yyyy", simplifyIfToday = true),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        CategoryPicker(
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (onCancel != null) {
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.cancel))
                }
            }
            Button(
                onClick = {
                    val value = amount ?: return@Button
                    onSave(name, value, period, nextDue, categoryId)
                },
                enabled = name.isNotBlank() && amount != null && amount > BigDecimal.ZERO,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
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
