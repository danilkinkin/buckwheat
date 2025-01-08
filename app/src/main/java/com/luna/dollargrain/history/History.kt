package com.luna.dollargrain.history

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luna.dollargrain.R
import com.luna.dollargrain.LocalWindowInsets
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.di.TUTORIAL_STAGE
import com.luna.dollargrain.di.TUTORS
import com.luna.dollargrain.editor.EditorViewModel
import com.luna.dollargrain.analytics.WholeBudgetCard
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.ui.colorEditor
import com.luna.dollargrain.data.ExtendCurrency
import com.luna.dollargrain.util.isSameDay
import com.luna.dollargrain.util.observeLiveData
import com.luna.dollargrain.util.toDate
import com.luna.dollargrain.util.toLocalDate
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import kotlin.math.abs
import kotlin.math.absoluteValue

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

    var historyList by remember { mutableStateOf<List<RowEntity>>(emptyList()) }
    val budget = spendsViewModel.budget.observeAsState(initial = BigDecimal.ZERO)
    val currency = spendsViewModel.currency.observeAsState(initial = ExtendCurrency.none())
    val startPeriodDate = spendsViewModel.startPeriodDate.observeAsState(initial = Date())
    val finishPeriodDate = spendsViewModel.finishPeriodDate.observeAsState(initial = Date())
    val scrollToBottom = remember { mutableStateOf(true) }
    val tutorial by appViewModel.getTutorialStage(TUTORS.SWIPE_EDIT_SPENT).observeAsState(TUTORIAL_STAGE.NONE)
    var isUserTrySwipe by remember { mutableStateOf(false) }

    observeLiveData(spendsViewModel.spends) { transactions ->
        val composedList = emptyList<RowEntity>().toMutableList()
        var lastSpentDate: LocalDate? = null
        var lastDayTotal: BigDecimal = BigDecimal.ZERO

        transactions
            .forEach { spent ->
                if (lastSpentDate === null || !isSameDay(
                        spent.date.time,
                        lastSpentDate!!.toDate().time
                    )
                ) {
                    if (lastSpentDate !== null) {
                        composedList.add(
                            RowEntity(
                                type = RowEntityType.DayTotal,
                                key = "total-${lastSpentDate}",
                                contentHash = "total-${lastSpentDate}",
                                transaction = null,
                                day = lastSpentDate!!,
                                dayTotal = lastDayTotal,
                            )
                        )
                    }

                    lastSpentDate = spent.date.toLocalDate()
                    lastDayTotal = BigDecimal.ZERO

                    composedList.add(
                        RowEntity(
                            type = RowEntityType.DayDivider,
                            key = "header-${lastSpentDate}",
                            contentHash = "header-${lastSpentDate}",
                            transaction = null,
                            day = lastSpentDate!!,
                            dayTotal = null,
                        )
                    )
                }

                lastDayTotal += spent.value

                composedList.add(
                    RowEntity(
                        type = RowEntityType.Spent,
                        key = "spent-${spent.uid}",
                        contentHash = "spent-${spent.uid}",
                        transaction = spent,
                        day = lastSpentDate!!,
                        dayTotal = null,
                    )
                )
            }

        if (transactions.isNotEmpty() && lastSpentDate !== null) {
            composedList.add(
                RowEntity(
                    type = RowEntityType.DayTotal,
                    key = "total-${lastSpentDate!!}",
                    contentHash = "total-${lastSpentDate}",
                    transaction = null,
                    day = lastSpentDate!!,
                    dayTotal = lastDayTotal,
                )
            )
        }

        historyList = composedList.toList().reversed().map { it }
    }

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
        animationSpec = TweenSpec(250), label = "",
    )

    val animatedList = updateAnimatedItemsState(newList = historyList)

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            LazyColumn(
                reverseLayout = true,
                state = scrollState
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
                        RowEntityType.DayDivider -> HistoryDateDivider(row.day)
                        RowEntityType.DayTotal -> TotalPerDay(
                            spentPerDay = row.dayTotal!!,
                            currency = currency.value,
                        )
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
                            val size = with(LocalDensity.current) {
                                java.lang.Float.max(
                                    java.lang.Float.min(
                                        16.dp.toPx(),
                                        abs(state.offset.value)
                                    ), 0f
                                ).toDp()
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
                                }, label = ""
                            )
                            val endCorners by animateDpAsState(
                                targetValue = when {
                                    state.dismissDirection == DismissDirection.EndToStart &&
                                            animateCorners -> 8.dp
                                    else -> 0.dp
                                }, label = ""
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
                                        currency = currency.value
                                    )
                                }
                            }
                        } else {
                            SpentItem(
                                transaction = row.transaction!!,
                                currency = currency.value
                            )
                        }
                    }
                }

                if (!readOnly) {
                    item("budget-info") {
                        WholeBudgetCard(
                            modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                            budget = budget.value,
                            currency = currency.value,
                            startDate = startPeriodDate.value,
                            finishDate = finishPeriodDate.value,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            ),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    LocalWindowInsets.current.calculateTopPadding()
                                )
                        )
                    }
                }
            }

            if (historyList.isEmpty()) {
                NoSpends(Modifier.weight(1f))
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

@Preview
@Composable
private fun PreviewDefault() {
    DollargrainTheme {
        History()
    }
}
