package com.danilkinkin.buckwheat.spendsHistory

import androidx.compose.foundation.layout.*
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
fun History(
    spendsViewModel: SpendsViewModel = viewModel(),
    appViewModel: AppViewModel = viewModel(),
) {
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isFirstRender by remember { mutableStateOf(true) }

    val spends by spendsViewModel.getSpends().observeAsState(initial = emptyList())
    val budget = spendsViewModel.budget.observeAsState()
    val startDate = spendsViewModel.startDate
    val finishDate = spendsViewModel.finishDate
    val key = spendsViewModel.key.observeAsState()

    DisposableEffect(Unit) {
        appViewModel.lockSwipeable.value = false

        onDispose { appViewModel.lockSwipeable.value = false }
    }

    DisposableEffect(key.value) { onDispose { } }

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
                BudgetInfo(
                    budget = budget.value ?: BigDecimal(0),
                    startDate = startDate,
                    finishDate = finishDate,
                    currency = spendsViewModel.currency,
                )
            }

            var lastDate: Date? = null

            spends.sortedBy { it.date }.forEach { item ->
                if (lastDate === null || !isSameDay(item.date.time, lastDate!!.time)) {
                    lastDate = item.date

                    item(item.date.time) {
                        HistoryDateDivider(item.date)
                    }
                }

                item(item.uid) {
                    Spent(
                        spent = item.copy(deleted = spendsViewModel.deletedSpends.contains(item.uid)),
                        currency = spendsViewModel.currency,
                        onDelete = {
                            spendsViewModel.removeSpent(item)
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


        if (spends.isEmpty()) {
            NoSpends(Modifier.weight(1f))
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