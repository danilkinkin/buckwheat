package com.danilkinkin.buckwheat.history

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CategoryChip
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.di.TUTORIAL_STAGE
import com.danilkinkin.buckwheat.di.TUTORS
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorEditor
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
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun History(
    modifier: Modifier = Modifier,
    spendsViewModel: SpendsViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel(),
    editorViewModel: EditorViewModel = viewModel(),
    readOnly: Boolean = false,
    onClose: () -> Unit = {}
) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val spends by spendsViewModel.spends.observeAsState(emptyList())
    val categories by spendsViewModel.categories.observeAsState(emptyList())
    val currency = spendsViewModel.currency.observeAsState(initial = ExtendCurrency.none())
    val scrollToBottom = remember { mutableStateOf(true) }
    val tutorial by appViewModel.getTutorialStage(TUTORS.SWIPE_EDIT_SPENT).observeAsState(TUTORIAL_STAGE.NONE)
    var isUserTrySwipe by remember { mutableStateOf(false) }
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

    DisposableEffect(Unit) {
        appViewModel.lockSwipeable.value = false
        scrollToBottom.value = true

        onDispose {
            appViewModel.lockSwipeable.value = false

            if (historyList.isNotEmpty() && isUserTrySwipe) {
                appViewModel.passTutorial(TUTORS.SWIPE_EDIT_SPENT)
            }

        }
    }

    val fapScale by animateFloatAsState(
        targetValue = if (appViewModel.lockSwipeable.value) 1f else 0f,
        animationSpec = TweenSpec(250),
    )

    val animatedList = updateAnimatedItemsState(newList = historyList)

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            if (!readOnly) {
                Spacer(Modifier.height(LocalWindowInsets.current.calculateTopPadding()))
                MonthHeader(
                    month = selectedMonth,
                    total = monthTotal,
                    currency = currency.value,
                    canGoNext = selectedMonth < YearMonth.now(),
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
            }

            Box(Modifier.weight(1f)) {
                if (historyList.isEmpty()) {
                    NoSpends(Modifier.fillMaxSize())
                } else {
                    LazyColumn(
                        reverseLayout = true,
                        state = scrollState,
                        modifier = Modifier.fillMaxSize(),
                    ) {

                // Fixer, because if scroll fast end-checker dispose
                item("spacer-2") {
                    Spacer(modifier = Modifier.height(2.dp))
                }

                item("end-checker") {
                    DisposableEffect(Unit) {
                        appViewModel.lockSwipeable.value = false

                        onDispose {
                            appViewModel.lockSwipeable.value = true
                        }
                    }
                }

                item("spacer") {
                    Spacer(modifier = Modifier.height(18.dp))
                }

                animatedItemsIndexed(
                    state = animatedList.value,
                    key = { rowItem -> rowItem.key },
                ) { index, row ->
                    when (row.type) {
                        RowEntityType.DayDivider -> HistoryDateDivider(
                            date = row.day,
                            dayTotal = row.dayTotal ?: BigDecimal.ZERO,
                            currency = currency.value,
                        )
                        // Day totals now live in the divider; old rows are no longer emitted.
                        RowEntityType.DayTotal -> Unit
                        RowEntityType.Spent -> if (!readOnly) SwipeActions(
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
                            onTried = { isUserTrySwipe = true },
                            showTutorial = index == 2 && tutorial === TUTORIAL_STAGE.READY_TO_SHOW,
                        ) { state ->
                            val localDensity = LocalDensity.current

                            // Quantised to whole dp: this feeds a RoundedCornerShape, so a
                            // per-frame value rebuilt the row shape on every drag frame.
                            val size by remember(localDensity) {
                                derivedStateOf {
                                    with(localDensity) {
                                        val px = min(16.dp.toPx(), abs(state.offset.value))
                                            .coerceAtLeast(0f)
                                        (px / density).roundToInt().dp
                                    }
                                }
                            }

                            val animateCorners by remember {
                                derivedStateOf {
                                    state.offset.value.absoluteValue > 30
                                }
                            }
                            val startCorners by animateDpAsState(
                                targetValue = when {
                                    state.dismissDirection == DismissDirection.StartToEnd &&
                                            animateCorners -> 8.dp
                                    else -> 0.dp
                                }
                            )
                            val endCorners by animateDpAsState(
                                targetValue = when {
                                    state.dismissDirection == DismissDirection.EndToStart &&
                                            animateCorners -> 8.dp
                                    else -> 0.dp
                                }
                            )

                            Box(
                                modifier = Modifier.height(IntrinsicSize.Min)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(
                                            vertical = min(
                                                size / 4f,
                                                4.dp
                                            )
                                        )
                                        .clip(RoundedCornerShape(size)),
                                    color = colorEditor,
                                    shape = RoundedCornerShape(
                                        topStart = startCorners,
                                        bottomStart = startCorners,
                                        topEnd = endCorners,
                                        bottomEnd = endCorners,
                                    ),
                                ) {
                                }
                                Box(
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    SpentItem(
                                        transaction = row.transaction!!,
                                        currency = currency.value,
                                        category = row.transaction!!.categoryId?.let { categoriesById[it] },
                                    )
                                }
                            }
                        } else {
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

        if (!readOnly) {
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
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )

                }
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    categories: List<Category>,
    selectedCategoryId: Int?,
    onSelect: (Int?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
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

    transactions.groupBy { it.date.toLocalDate() }.forEach { (day, daySpends) ->
        val dayTotal = daySpends.fold(BigDecimal.ZERO) { total, spend -> total + spend.value }

        rows.add(
            RowEntity(
                type = RowEntityType.DayDivider,
                key = "header-$day",
                contentHash = "header-$day-$dayTotal",
                transaction = null,
                day = day,
                dayTotal = dayTotal,
            )
        )

        daySpends.forEach { spend ->
            rows.add(
                RowEntity(
                    type = RowEntityType.Spent,
                    key = "spent-${spend.uid}",
                    contentHash = "spent-${spend.uid}",
                    transaction = spend,
                    day = day,
                    dayTotal = null,
                )
            )
        }
    }

    // reverseLayout renders index 0 at the bottom, so newest rows must come first.
    return rows.reversed()
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    total: BigDecimal,
    currency: ExtendCurrency,
    canGoNext: Boolean,
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
        IconButton(onClick = onPrevious) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = null,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
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
        IconButton(onClick = onNext, enabled = canGoNext) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
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
