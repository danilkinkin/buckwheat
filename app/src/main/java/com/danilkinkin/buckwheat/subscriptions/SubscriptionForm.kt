package com.danilkinkin.buckwheat.subscriptions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CategoryPicker
import com.danilkinkin.buckwheat.base.MinimalTextField
import com.danilkinkin.buckwheat.data.ServiceCatalog
import com.danilkinkin.buckwheat.data.defaultCategoryName
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SubscriptionForm(
    initial: Subscription?,
    categories: List<Category>,
    onSave: (
        name: String,
        amount: BigDecimal,
        period: SubscriptionPeriod,
        intervalCount: Int,
        nextDue: Date,
        categoryId: Int?,
        serviceId: String?,
    ) -> Unit,
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
    var serviceId by remember { mutableStateOf(initial?.serviceId) }
    val service = ServiceCatalog.find(serviceId)
    var amountText by remember { mutableStateOf(initial?.amount?.toPlainString() ?: "") }
    var period by remember {
        mutableStateOf(initial?.period?.toSubscriptionPeriod() ?: SubscriptionPeriod.MONTHS)
    }
    var intervalCountText by remember {
        mutableStateOf((initial?.intervalCount ?: 1).toString())
    }
    val intervalCount = intervalCountText.toIntOrNull()?.coerceIn(1, 999) ?: 1
    var customInterval by remember {
        mutableStateOf(
            !isPresetInterval(
                count = initial?.intervalCount ?: 1,
                period = initial?.period?.toSubscriptionPeriod() ?: SubscriptionPeriod.MONTHS,
            )
        )
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
        ServicePicker(
            selectedId = serviceId,
            onSelect = { picked ->
                val previousName = service?.name
                serviceId = picked?.id

                // Keep a custom label, but follow the service name while the field still has
                // the previous (or no) service name in it.
                if (picked != null && (name.isBlank() || name == previousName)) {
                    name = picked.name
                }

                // Follow the service's purpose while the category is still untouched or just
                // the generic Other. A deliberately chosen category always wins.
                val currentCategoryIsOther =
                    categories.firstOrNull { it.id == categoryId }?.name == "Other"

                if (picked != null && (!categoryTouched || currentCategoryIsOther)) {
                    categories
                        .firstOrNull { it.name == picked.defaultCategoryName }
                        ?.let { categoryId = it.id }
                }
            },
        )

        Spacer(Modifier.height(10.dp))

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

        Text(
            text = stringResource(R.string.billing_period),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )

        Spacer(Modifier.height(6.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DurationChip(
                label = stringResource(R.string.duration_1m),
                selected = !customInterval && intervalCount == 1 && period == SubscriptionPeriod.MONTHS,
            ) {
                period = SubscriptionPeriod.MONTHS
                intervalCountText = "1"
                customInterval = false
            }
            DurationChip(
                label = stringResource(R.string.duration_3m),
                selected = !customInterval && intervalCount == 3 && period == SubscriptionPeriod.MONTHS,
            ) {
                period = SubscriptionPeriod.MONTHS
                intervalCountText = "3"
                customInterval = false
            }
            DurationChip(
                label = stringResource(R.string.duration_6m),
                selected = !customInterval && intervalCount == 6 && period == SubscriptionPeriod.MONTHS,
            ) {
                period = SubscriptionPeriod.MONTHS
                intervalCountText = "6"
                customInterval = false
            }
            DurationChip(
                label = stringResource(R.string.duration_1y),
                selected = !customInterval && intervalCount == 1 && period == SubscriptionPeriod.YEARS,
            ) {
                period = SubscriptionPeriod.YEARS
                intervalCountText = "1"
                customInterval = false
            }
            DurationChip(
                label = stringResource(R.string.duration_other),
                selected = customInterval,
            ) {
                customInterval = true
            }
        }

        if (customInterval) {
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.every),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(10.dp))
                MinimalTextField(
                    value = intervalCountText,
                    onValueChange = { value ->
                        intervalCountText = value.filter { it.isDigit() }.take(3)
                    },
                    placeholder = "1",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.width(92.dp),
                    fillWidth = false,
                )
            }

            Spacer(Modifier.height(10.dp))

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
                        // Four segments plus the default check icon leaves no room for
                        // "Months"; the label is the selection indicator here.
                        icon = {},
                        label = {
                            Text(
                                text = option.unitLabel(),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                    )
                }
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
                    onSave(name, value, period, intervalCount, nextDue, categoryId, serviceId)
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

private fun isPresetInterval(count: Int, period: SubscriptionPeriod?): Boolean =
    (period == SubscriptionPeriod.MONTHS && count in listOf(1, 3, 6)) ||
            (period == SubscriptionPeriod.YEARS && count == 1)

@Composable
private fun DurationChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}
