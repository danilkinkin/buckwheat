package com.danilkinkin.buckwheat.spendsHistory

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.Collapse
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.entities.Spent
import com.danilkinkin.buckwheat.finishPeriod.WholeBudgetCard
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.isSameDay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*

@Composable
fun History(
    modifier: Modifier = Modifier,
    spendsViewModel: SpendsViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel(),
) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isFirstRender by remember { mutableStateOf(true) }

    val spends by spendsViewModel.getSpends().observeAsState(initial = emptyList())
    val budget = spendsViewModel.budget.observeAsState(initial = BigDecimal(0))
    val startDate = spendsViewModel.startDate.observeAsState(initial = Date())
    val finishDate = spendsViewModel.finishDate.observeAsState(initial = Date())

    DisposableEffect(Unit) {
        appViewModel.lockSwipeable.value = false

        onDispose { appViewModel.lockSwipeable.value = false }
    }

    DisposableEffect(Unit) {
        onDispose {
            spendsViewModel.commitDeletedSpends()
        }
    }

    val fapScale by animateFloatAsState(
        targetValue = if (appViewModel.lockSwipeable.value) 1f else 0f,
        animationSpec = TweenSpec(250),
    )

    var lastDate: Date? = null
    val spendsPerDay = remember(spends) {
        val list = emptyList<MutableList<Spent>>().toMutableList()

        spends.forEach { spent ->
            if (lastDate === null || !isSameDay(spent.date.time, lastDate!!.time)) {
                lastDate = spent.date

                list.add(emptyList<Spent>().toMutableList())
            }

            list.last().add(spent)
        }

        return@remember list
    }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            LazyColumn(state = scrollState) {
                item("budget-info") {
                    DisposableEffect(spends.size) {
                        if (spends.isEmpty() || !isFirstRender) return@DisposableEffect onDispose {  }

                        isFirstRender = false

                        coroutineScope.launch {
                            scrollState.scrollToItem(spends.size + 1)
                        }

                        onDispose { }
                    }

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

                spendsPerDay.forEach { spends ->
                    var spentPerDay = BigDecimal(0)
                    val date = spends.first().date
                    val isAllDeleted = spends.find { !it.deleted } === null

                    item("header-${date.time}") {
                        Collapse(show = !isAllDeleted) {
                            HistoryDateDivider(date)
                        }
                    }

                    spends.forEach { spent ->
                        item(spent.uid) {
                            Spent(
                                spent = spent,
                                currency = spendsViewModel.currency.value!!,
                                onDelete = {
                                    spendsViewModel.removeSpent(spent)
                                }
                            )
                        }
                        if (!spent.deleted) {
                            spentPerDay += spent.value
                        }
                    }

                    item("total-${date.time}") {
                        Collapse(show = !isAllDeleted) {
                            TotalPerDay(
                                spentPerDay = spentPerDay,
                                currency = spendsViewModel.currency.value!!,
                            )
                        }
                    }
                }

                item("spacer") {
                    Spacer(modifier = Modifier.height(20.dp))
                }

                item("end-checker") {
                    DisposableEffect(Unit) {
                        appViewModel.lockSwipeable.value = false

                        onDispose { appViewModel.lockSwipeable.value = true }
                    }
                }
            }


            if (spends.none { !it.deleted }) {
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
                        scrollState.animateScrollToItem(spends.size + 1)
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