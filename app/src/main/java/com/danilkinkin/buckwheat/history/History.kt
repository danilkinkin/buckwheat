package com.danilkinkin.buckwheat.history

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.finishPeriod.WholeBudgetCard
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorEditor
import com.danilkinkin.buckwheat.util.isSameDay
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
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
    onClose: () -> Unit = {}
) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val spends by spendsViewModel.getSpends().observeAsState(initial = null)
    val budget = spendsViewModel.budget.observeAsState(initial = BigDecimal(0))
    val startDate = spendsViewModel.startDate.observeAsState(initial = Date())
    val finishDate = spendsViewModel.finishDate.observeAsState(initial = Date())
    val scrollToBottom = remember { mutableStateOf(true) }
    val showSwipeTutorial = remember {
        appViewModel.getBooleanValue("tutorialSwipe", true)
    }

    DisposableEffect(Unit) {
        appViewModel.lockSwipeable.value = false
        scrollToBottom.value = true

        onDispose {
            appViewModel.lockSwipeable.value = false

            if (spends !== null && spends!!.isNotEmpty()) {
                appViewModel.setBooleanValue("tutorialSwipe", false)
            }
        }
    }

    val fapScale by animateFloatAsState(
        targetValue = if (appViewModel.lockSwipeable.value) 1f else 0f,
        animationSpec = TweenSpec(250),
    )

    val animatedList = if (spends !== null) {
        val list = emptyList<RowEntity>().toMutableList()

        var lastSpentDate: LocalDate? = null
        var lastDayTotal: BigDecimal = BigDecimal.ZERO

        spends!!
            .forEach { spent ->
                if (lastSpentDate === null || !isSameDay(
                        spent.date.time,
                        lastSpentDate!!.toDate().time
                    )
                ) {
                    if (lastSpentDate !== null) {
                        list.add(
                            RowEntity(
                                type = RowEntityType.DayTotal,
                                key = "total-${lastSpentDate}",
                                contentHash = "$lastDayTotal",
                                spent = null,
                                day = lastSpentDate!!,
                                dayTotal = lastDayTotal,
                            )
                        )
                    }

                    lastSpentDate = spent.date.toLocalDate()
                    lastDayTotal = BigDecimal.ZERO

                    list.add(
                        RowEntity(
                            type = RowEntityType.DayDivider,
                            key = "header-${lastSpentDate}",
                            spent = null,
                            day = lastSpentDate!!,
                            dayTotal = null,
                        )
                    )
                }

                lastDayTotal += spent.value

                list.add(
                    RowEntity(
                        type = RowEntityType.Spent,
                        key = "spent-${spent.uid}",
                        spent = spent,
                        day = lastSpentDate!!,
                        dayTotal = null,
                    )
                )
            }

        if (spends!!.isNotEmpty() && lastSpentDate !== null) {
            list.add(
                RowEntity(
                    type = RowEntityType.DayTotal,
                    key = "total-${lastSpentDate!!}",
                    contentHash = "$lastDayTotal",
                    spent = null,
                    day = lastSpentDate!!,
                    dayTotal = lastDayTotal,
                )
            )
        }


        updateAnimatedItemsState(newList = list.toList().reversed().map { it })
    } else {
        null
    }

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

                if (animatedList !== null) animatedItemsIndexed(
                    state = animatedList.value,
                    key = { rowItem -> rowItem.key },
                ) { index, row ->
                    when (row.type) {
                        RowEntityType.DayDivider -> HistoryDateDivider(row.day)
                        RowEntityType.DayTotal -> TotalPerDay(
                            spentPerDay = row.dayTotal!!,
                            currency = spendsViewModel.currency.value!!,
                        )
                        RowEntityType.Spent -> SwipeActions(
                            startActionsConfig = SwipeActionsConfig(
                                threshold = 0.4f,
                                background = MaterialTheme.colorScheme.tertiaryContainer,
                                backgroundActive = MaterialTheme.colorScheme.tertiary,
                                iconTint = MaterialTheme.colorScheme.onTertiary,
                                icon = painterResource(R.drawable.ic_edit),
                                stayDismissed = true,
                                onDismiss = {
                                    spendsViewModel.editSpent(row.spent!!)
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
                                    spendsViewModel.removeSpent(row.spent!!)
                                }
                            ),
                            showTutorial = index == animatedList.value.size - 2 && showSwipeTutorial
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
                                        spent = row.spent!!,
                                        currency = spendsViewModel.currency.value!!
                                    )
                                }
                            }
                        }
                    }
                }

                item("budget-info") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                WindowInsets.systemBars
                                    .asPaddingValues()
                                    .calculateTopPadding()
                            )
                    )

                    WholeBudgetCard(
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        budget = budget.value,
                        currency = spendsViewModel.currency.value!!,
                        startDate = startDate.value,
                        finishDate = finishDate.value,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        ),
                    )
                }
            }
            
            if (spends !== null && spends!!.isEmpty()) {
                NoSpends(Modifier.weight(1f))
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
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )

            }
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