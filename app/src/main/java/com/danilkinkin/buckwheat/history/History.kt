package com.danilkinkin.buckwheat.history

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CategoryChip
import com.danilkinkin.buckwheat.base.ListCard
import com.danilkinkin.buckwheat.base.MinimalTextField
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.ServiceCatalog
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyYearMonth
import com.danilkinkin.buckwheat.util.toLocalDate
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterialApi::class, ExperimentalLayoutApi::class)
@Composable
fun History(
    modifier: Modifier = Modifier,
    spendsViewModel: SpendsViewModel = viewModel(),
    editorViewModel: EditorViewModel = viewModel(),
    onBack: (() -> Unit)? = null,
    onClose: () -> Unit = {}
) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val spends by spendsViewModel.spends.observeAsState(emptyList())
    val categories by spendsViewModel.categories.observeAsState(emptyList())
    val currency = spendsViewModel.currency.observeAsState(initial = ExtendCurrency.none())

    var selectedCategoryIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var onlySubscriptions by remember { mutableStateOf(false) }
    var minAmount by remember { mutableStateOf("") }
    var maxAmount by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }

    val categoriesById = remember(categories) { categories.associateBy { it.id } }
    val monthSpends = remember(spends, selectedMonth) {
        spends.filter { YearMonth.from(it.date.toLocalDate()) == selectedMonth }
    }

    LaunchedEffect(categories) {
        val validIds = categories.mapTo(mutableSetOf()) { it.id }
        selectedCategoryIds = selectedCategoryIds.filterNot { it !in validIds }.toSet()
    }

    val filteredSpends = remember(
        monthSpends,
        selectedCategoryIds,
        onlySubscriptions,
        minAmount,
        maxAmount,
        query,
        categoriesById,
    ) {
        val min = minAmount.toBigDecimalOrNull()
        val max = maxAmount.toBigDecimalOrNull()
        val needle = query.trim().lowercase()

        monthSpends.filter { transaction ->
            val categoryId = transaction.categoryId
            (selectedCategoryIds.isEmpty() || categoryId in selectedCategoryIds) &&
                    (!onlySubscriptions || transaction.serviceId != null) &&
                    (min == null || transaction.value >= min) &&
                    (max == null || transaction.value <= max) &&
                    (needle.isEmpty() || matchesQuery(transaction, needle, categoriesById))
        }
    }

    val filteredTotal = remember(filteredSpends) {
        filteredSpends.fold(BigDecimal.ZERO) { total, transaction -> total + transaction.value }
    }
    val dailyTotals = remember(filteredSpends, selectedMonth) {
        val byDay = filteredSpends.groupBy { it.date.toLocalDate().dayOfMonth }
        (1..selectedMonth.lengthOfMonth()).map { day ->
            byDay[day]?.fold(BigDecimal.ZERO) { total, transaction -> total + transaction.value }
                ?: BigDecimal.ZERO
        }
    }
    val daysElapsed = if (selectedMonth == YearMonth.now()) {
        LocalDate.now().dayOfMonth
    } else {
        selectedMonth.lengthOfMonth()
    }
    val dailyAverage = remember(filteredTotal, daysElapsed) {
        if (daysElapsed <= 0) BigDecimal.ZERO
        else filteredTotal.divide(BigDecimal(daysElapsed), 0, RoundingMode.HALF_UP)
    }
    val historyList = remember(filteredSpends) { composeHistoryRows(filteredSpends) }

    val filtersActive = selectedCategoryIds.isNotEmpty() ||
            onlySubscriptions ||
            minAmount.isNotBlank() ||
            maxAmount.isNotBlank()

    fun clearAllFilters() {
        selectedCategoryIds = emptySet()
        onlySubscriptions = false
        minAmount = ""
        maxAmount = ""
        query = ""
    }

    val showJump by remember { derivedStateOf { scrollState.firstVisibleItemIndex > 0 } }
    val fapScale by animateFloatAsState(
        targetValue = if (showJump) 1f else 0f,
        animationSpec = TweenSpec(250),
    )

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(LocalWindowInsets.current.calculateTopPadding()))
            MonthHeader(
                month = selectedMonth,
                canGoNext = selectedMonth < YearMonth.now(),
                onBack = onBack,
                onSearch = { showSearch = !showSearch },
                searchActive = showSearch,
                onFilters = { showFilters = true },
                filtersActive = filtersActive,
                onPrevious = { selectedMonth = selectedMonth.minusMonths(1) },
                onNext = {
                    if (selectedMonth < YearMonth.now()) {
                        selectedMonth = selectedMonth.plusMonths(1)
                    }
                },
            )

            MonthlySummaryCard(
                total = filteredTotal,
                count = filteredSpends.size,
                dailyAverage = dailyAverage,
                dailyTotals = dailyTotals,
                currency = currency.value,
                filtered = filtersActive || query.isNotBlank(),
            )

            if (showSearch) {
                SearchField(
                    value = query,
                    onValueChange = { query = it },
                )
            }

            if (categories.isNotEmpty()) {
                CategoryFilterRow(
                    categories = categories,
                    selectedCategoryIds = selectedCategoryIds,
                    onToggle = { categoryId ->
                        selectedCategoryIds = if (categoryId in selectedCategoryIds) {
                            selectedCategoryIds - categoryId
                        } else {
                            selectedCategoryIds + categoryId
                        }
                    },
                    onClear = { selectedCategoryIds = emptySet() },
                )
            }

            if (filtersActive || query.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { clearAllFilters() }) {
                        Text(stringResource(R.string.clear_filters))
                    }
                }
            }

            Box(Modifier.weight(1f)) {
                when {
                    monthSpends.isEmpty() -> NoSpends(Modifier.fillMaxSize())

                    historyList.isEmpty() -> NoMatches(
                        modifier = Modifier.fillMaxSize(),
                        onClear = { clearAllFilters() },
                    )

                    else -> LazyColumn(
                        state = scrollState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = 96.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(
                            items = historyList,
                            key = { rowItem -> rowItem.key },
                        ) { row ->
                            when (row.type) {
                                RowEntityType.DayDivider -> HistoryDateDivider(
                                    date = row.day,
                                    dayTotal = row.dayTotal ?: BigDecimal.ZERO,
                                    currency = currency.value,
                                    dayCount = row.dayCount ?: 0,
                                )

                                // Day totals now live in the divider; old rows are no longer emitted.
                                RowEntityType.DayTotal -> Unit

                                RowEntityType.Spent -> SwipeActions(
                                    modifier = Modifier.clip(RoundedCornerShape(24.dp)),
                                    startActionsConfig = SwipeActionsConfig(
                                        threshold = 0.4f,
                                        background = MaterialTheme.colorScheme.tertiaryContainer,
                                        backgroundActive = MaterialTheme.colorScheme.tertiary,
                                        iconTint = MaterialTheme.colorScheme.onTertiary,
                                        icon = painterResource(R.drawable.ic_edit),
                                        stayDismissed = false,
                                        onDismiss = {
                                            editorViewModel.startEditingSpent(row.transaction!!)
                                            onClose()
                                        }
                                    ),
                                    endActionsConfig = SwipeActionsConfig(
                                        threshold = 0.4f,
                                        background = MaterialTheme.colorScheme.errorContainer,
                                        backgroundActive = MaterialTheme.colorScheme.error,
                                        iconTint = MaterialTheme.colorScheme.onError,
                                        icon = painterResource(R.drawable.ic_delete_forever),
                                        stayDismissed = true,
                                        onDismiss = {
                                            spendsViewModel.removeSpent(row.transaction!!)
                                        }
                                    ),
                                ) {
                                    ListCard(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(24.dp))
                                            .clickable {
                                                editorViewModel.startEditingSpent(row.transaction!!)
                                                onClose()
                                            },
                                    ) {
                                        SpentItem(
                                            transaction = row.transaction!!,
                                            currency = currency.value,
                                            category = row.transaction!!.categoryId?.let { categoriesById[it] },
                                            service = ServiceCatalog.find(row.transaction!!.serviceId),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            FloatingActionButton(
                modifier = Modifier
                    .padding(end = 24.dp, bottom = 32.dp)
                    .scale(fapScale),
                onClick = {
                    coroutineScope.launch {
                        scrollState.animateScrollToItem(0)
                    }
                },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_down),
                    contentDescription = null,
                    modifier = Modifier
                        .size(ButtonDefaults.IconSize)
                        .rotate(180f),
                )
            }
        }
    }

    if (showFilters) {
        FiltersDialog(
            onlySubscriptions = onlySubscriptions,
            minAmount = minAmount,
            maxAmount = maxAmount,
            onDismiss = { showFilters = false },
            onApply = { only, min, max ->
                onlySubscriptions = only
                minAmount = min
                maxAmount = max
                showFilters = false
            },
        )
    }
}

private fun matchesQuery(
    transaction: Transaction,
    query: String,
    categoriesById: Map<Int, Category>,
): Boolean {
    val category = transaction.categoryId
        ?.let { categoriesById[it] }
        ?.name
        ?.lowercase()
        .orEmpty()
    val service = ServiceCatalog.find(transaction.serviceId)?.name?.lowercase().orEmpty()
    val amount = transaction.value.toPlainString()

    return transaction.comment.lowercase().contains(query) ||
            category.contains(query) ||
            service.contains(query) ||
            amount.contains(query)
}

@Composable
private fun MonthlySummaryCard(
    total: BigDecimal,
    count: Int,
    dailyAverage: BigDecimal,
    dailyTotals: List<BigDecimal>,
    currency: ExtendCurrency,
    filtered: Boolean,
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
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            if (filtered) R.string.filtered_total else R.string.total_spent
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = numberFormat(context, total, currency),
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Text(
                        text = pluralStringResource(R.plurals.expenses_count, count, count),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Sparkline(dailyTotals)

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${stringResource(R.string.daily_average)} " +
                        numberFormat(context, dailyAverage, currency),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun Sparkline(dailyTotals: List<BigDecimal>) {
    val max = dailyTotals.maxOfOrNull { it } ?: BigDecimal.ZERO

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        dailyTotals.forEach { amount ->
            val fraction = if (max > BigDecimal.ZERO) {
                amount.toFloat() / max.toFloat()
            } else {
                0f
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(fraction.coerceAtLeast(0.06f))
                    .clip(RoundedCornerShape(2.dp))
                    .then(
                        Modifier.background(
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                alpha = if (amount > BigDecimal.ZERO) 0.55f else 0.12f
                            )
                        )
                    ),
            )
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f)) {
            MinimalTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = stringResource(R.string.search_expenses),
            )
        }
        if (value.isNotEmpty()) {
            IconButton(onClick = { onValueChange("") }) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun NoMatches(
    modifier: Modifier = Modifier,
    onClear: () -> Unit,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_filter),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.no_matches),
            style = MaterialTheme.typography.bodyMedium,
            color = colorOnEditor.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onClear) {
            Text(stringResource(R.string.clear_filters))
        }
    }
}

@Composable
private fun FiltersDialog(
    onlySubscriptions: Boolean,
    minAmount: String,
    maxAmount: String,
    onDismiss: () -> Unit,
    onApply: (Boolean, String, String) -> Unit,
) {
    var localOnly by remember { mutableStateOf(onlySubscriptions) }
    var localMin by remember { mutableStateOf(minAmount) }
    var localMax by remember { mutableStateOf(maxAmount) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    text = stringResource(R.string.filters),
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.only_subscriptions),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(
                        checked = localOnly,
                        onCheckedChange = { localOnly = it },
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.amount_range),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MinimalTextField(
                        value = localMin,
                        onValueChange = { localMin = it.filter { c -> c.isDigit() || c == '.' } },
                        placeholder = stringResource(R.string.min_amount),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(130.dp),
                        fillWidth = false,
                    )
                    MinimalTextField(
                        value = localMax,
                        onValueChange = { localMax = it.filter { c -> c.isDigit() || c == '.' } },
                        placeholder = stringResource(R.string.max_amount),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.width(130.dp),
                        fillWidth = false,
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = {
                            localOnly = false
                            localMin = ""
                            localMax = ""
                        },
                    ) {
                        Text(stringResource(R.string.clear_filters))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onApply(localOnly, localMin, localMax) }) {
                        Text(stringResource(R.string.apply))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryFilterRow(
    categories: List<Category>,
    selectedCategoryIds: Set<Int>,
    onToggle: (Int) -> Unit,
    onClear: () -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CategoryChip(
            text = stringResource(R.string.all_categories),
            selected = selectedCategoryIds.isEmpty(),
            onClick = onClear,
        )
        categories.forEach { category ->
            CategoryChip(
                text = category.name,
                emoji = category.emoji,
                selected = category.id in selectedCategoryIds,
                onClick = { onToggle(category.id) },
            )
        }
    }
}

private fun composeHistoryRows(transactions: List<Transaction>): List<RowEntity> {
    val rows = mutableListOf<RowEntity>()

    // Newest day first, and the newest spend of that day first: the list is read top-down.
    transactions.groupBy { it.date.toLocalDate() }.entries.reversed().forEach { (day, daySpends) ->
        val dayTotal = daySpends.fold(BigDecimal.ZERO) { total, spend -> total + spend.value }

        rows.add(
            RowEntity(
                type = RowEntityType.DayDivider,
                key = "header-$day",
                transaction = null,
                day = day,
                dayTotal = dayTotal,
                dayCount = daySpends.size,
            )
        )

        daySpends.reversed().forEach { spend ->
            rows.add(
                RowEntity(
                    type = RowEntityType.Spent,
                    key = "spent-${spend.uid}",
                    transaction = spend,
                    day = day,
                    dayTotal = null,
                )
            )
        }
    }

    return rows
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    canGoNext: Boolean,
    onBack: (() -> Unit)? = null,
    onSearch: () -> Unit,
    searchActive: Boolean,
    onFilters: () -> Unit,
    filtersActive: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = null,
                )
            }
        }
        Text(
            text = prettyYearMonth(month),
            style = MaterialTheme.typography.titleLarge,
            color = colorOnEditor,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )
        IconButton(onClick = onSearch) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = stringResource(R.string.search_expenses),
                tint = if (searchActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
        IconButton(onClick = onFilters) {
            Icon(
                painter = painterResource(R.drawable.ic_filter),
                contentDescription = stringResource(R.string.filters),
                tint = if (filtersActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
        IconButton(onClick = onPrevious) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_left),
                contentDescription = null,
            )
        }
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        History()
    }
}
