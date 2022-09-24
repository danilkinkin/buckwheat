package com.danilkinkin.buckwheat.spendsHistory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.isSameDay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*

@Composable
fun History(spendsViewModel: SpendsViewModel = viewModel(), appViewModel: AppViewModel = viewModel()) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isFirstRender by remember { mutableStateOf(true) }

    val spends = spendsViewModel.getSpends().observeAsState(initial = emptyList())
    val budget = spendsViewModel.budget.observeAsState()
    val startDate = spendsViewModel.startDate
    val finishDate = spendsViewModel.finishDate

    DisposableEffect(Unit) {
        appViewModel.lockSwipeable.value = false

        onDispose { appViewModel.lockSwipeable.value = false }
    }

    Box {
        LazyColumn(
            state = scrollState,
        ) {
            item("budgetInfo") {
                DisposableEffect(spends.value.size) {
                    if (spends.value.isEmpty() || !isFirstRender) return@DisposableEffect onDispose {  }

                    isFirstRender = false

                    coroutineScope.launch {
                        scrollState.scrollToItem(spends.value.size + 1)
                    }

                    onDispose { }
                }
                BudgetInfo(
                    budget = budget.value ?: BigDecimal(0),
                    startDate = startDate,
                    finishDate = finishDate,
                    currency = spendsViewModel.currency,
                )
            }

            var lastDate: Date? = null

            spends.value.forEach {
                if (lastDate === null || !isSameDay(it.date.time, lastDate!!.time)) {
                    lastDate = it.date

                    item(it.date.time) {
                        HistoryDateDivider(it.date)
                    }
                }
                item(it.uid) {
                    Spent(
                        spent = it,
                        currency = spendsViewModel.currency,
                        onDelete = {
                            spendsViewModel.removeSpent(it)
                        }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            item {
                DisposableEffect(Unit) {
                    appViewModel.lockSwipeable.value = false

                    onDispose { appViewModel.lockSwipeable.value = true }
                }
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