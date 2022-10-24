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

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            LazyColumn(state = scrollState) {
                item("budgetInfo") {
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

                var lastDate: Date? = null

                spends.forEach { item ->
                    if (lastDate === null || !isSameDay(item.date.time, lastDate!!.time)) {
                        lastDate = item.date

                        item(item.date.time) {
                            Collapse(show = hasNextSpendsInThisDay(spends, item) || !item.deleted) {
                                HistoryDateDivider(item.date)
                            }
                        }
                    }

                    item(item.uid) {
                        Spent(
                            spent = item,
                            currency = spendsViewModel.currency.value!!,
                            onDelete = {
                                spendsViewModel.removeSpent(item)
                            }
                        )
                    }
                }
                item("spacer") {
                    Spacer(modifier = Modifier.height(20.dp))
                }
                item("endChecker") {
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

fun hasNextSpendsInThisDay(spends: List<Spent>, currSpent: Spent): Boolean {
    var startIndex = -1
    var hasSpent = false

    spends.forEachIndexed { index, spent ->
        if (spent.uid == currSpent.uid) startIndex = index
        else if (startIndex != -1) {
            if (isSameDay(currSpent.date.time, spent.date.time)) hasSpent = !spent.deleted || hasSpent
            else return@forEachIndexed            
        }
    }
    
    return hasSpent
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        History()
    }
}