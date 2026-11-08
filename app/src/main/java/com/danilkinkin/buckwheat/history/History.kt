package com.danilkinkin.buckwheat.history

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CategoryChip
import com.danilkinkin.buckwheat.base.ListCard
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyYearMonth
import com.danilkinkin.buckwheat.util.toLocalDate
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
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
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }

    LaunchedEffect(categories, selectedCategoryId) {
        val categoryId = selectedCategoryId
        if (categoryId != null && categories.none { it.id == categoryId }) {
            selectedCategoryId = null
        }
    }

    val categoriesById = remember(categories) { categories.associateBy { it.id } }
    val monthSpends = remember(spends, selectedMonth) {
        spends.filter { YearMonth.from(it.date.toLocalDate()) == selectedMonth }
    }
    val filteredSpends = remember(monthSpends, selectedCategoryId) {
        val categoryId = selectedCategoryId
        if (categoryId == null) monthSpends else monthSpends.filter { it.categoryId == categoryId }
    }
    val monthTotal = remember(filteredSpends) {
        filteredSpends.fold(BigDecimal.ZERO) { total, transaction -> total + transaction.value }
    }
    val historyList = remember(filteredSpends) { composeHistoryRows(filteredSpends) }

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
                total = monthTotal,
                currency = currency.value,
                canGoNext = selectedMonth < YearMonth.now(),
                onBack = onBack,
                onPrevious = { selectedMonth = selectedMonth.minusMonths(1) },
                onNext = {
                    if (selectedMonth < YearMonth.now()) {
                        selectedMonth = selectedMonth.plusMonths(1)
                    }
                },
            )
            if (categories.isNotEmpty()) {
                CategoryFilterRow(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onSelect = { selectedCategoryId = it },
                )
            }

            Box(Modifier.weight(1f)) {
                if (historyList.isEmpty()) {
                    NoSpends(Modifier.fillMaxSize())
                } else {
                    LazyColumn(
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
                                    ListCard {
                                        SpentItem(
                                            transaction = row.transaction!!,
                                            currency = currency.value,
                                            category = row.transaction!!.categoryId?.let { categoriesById[it] },
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryFilterRow(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onSelect: (Int?) -> Unit,
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
            selected = selectedCategoryId == null,
            onClick = { onSelect(null) },
        )
        categories.forEach { category ->
            CategoryChip(
                text = category.name,
                emoji = category.emoji,
                selected = category.id == selectedCategoryId,
                onClick = { onSelect(category.id) },
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
    total: BigDecimal,
    currency: ExtendCurrency,
    canGoNext: Boolean,
    onBack: (() -> Unit)? = null,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val context = LocalContext.current

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
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = prettyYearMonth(month),
                style = MaterialTheme.typography.titleLarge,
                color = colorOnEditor,
            )
            Text(
                text = numberFormat(context, total, currency),
                style = MaterialTheme.typography.labelMedium,
                color = colorOnEditor.copy(alpha = 0.6f),
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
