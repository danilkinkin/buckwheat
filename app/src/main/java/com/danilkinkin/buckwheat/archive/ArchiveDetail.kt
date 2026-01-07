package com.danilkinkin.buckwheat.archive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.analytics.WholeBudgetCard
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.data.ArchiveViewModel
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.entities.ArchivedTransaction
import com.danilkinkin.buckwheat.data.entities.Period
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.history.HistoryDateDivider
import com.danilkinkin.buckwheat.history.TotalPerDay
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.isSameDay
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Date

const val ARCHIVE_DETAIL_SHEET = "archiveDetail"

sealed class ArchivedRowEntity {
    data class DayDivider(val day: LocalDate) : ArchivedRowEntity()
    data class Spent(val transaction: ArchivedTransaction) : ArchivedRowEntity()
    data class DayTotal(val day: LocalDate, val total: BigDecimal) : ArchivedRowEntity()
}

@Composable
fun ArchiveDetail(
    periodId: Int,
    archiveViewModel: ArchiveViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current
    val navigationBarHeight = LocalWindowInsets.current.calculateBottomPadding().coerceAtLeast(16.dp)

    val period by archiveViewModel.getPeriodById(periodId).observeAsState(null)
    val transactions by archiveViewModel.getTransactionsForPeriod(periodId).observeAsState(emptyList())

    // Filter only SPENT transactions
    val spends = remember(transactions) {
        transactions.filter { it.type == TransactionType.SPENT }
    }

    // Build history list
    val historyList = remember(spends) {
        buildHistoryList(spends)
    }

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { onClose() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.weight(1F))
                Text(
                    text = stringResource(R.string.period_details_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(Modifier.weight(1F))
                Spacer(Modifier.width(48.dp))
            }

            if (period == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val currency = if (period!!.currency.isNotEmpty()) {
                    ExtendCurrency.getInstance(period!!.currency)
                } else {
                    ExtendCurrency.none()
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = navigationBarHeight
                    ),
                ) {
                    // Budget overview card
                    item {
                        WholeBudgetCard(
                            budget = period!!.budget,
                            currency = currency,
                            startDate = period!!.startDate,
                            finishDate = period!!.finishDate,
                            actualFinishDate = period!!.actualFinishDate,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Spent summary
                    item {
                        SpentSummaryCard(period = period!!, currency = currency)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Section header
                    if (spends.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.history_title),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }

                    // Transaction history
                    items(historyList, key = {
                        when (it) {
                            is ArchivedRowEntity.DayDivider -> "divider-${it.day}"
                            is ArchivedRowEntity.Spent -> "spent-${it.transaction.uid}"
                            is ArchivedRowEntity.DayTotal -> "total-${it.day}"
                        }
                    }) { item ->
                        when (item) {
                            is ArchivedRowEntity.DayDivider -> {
                                HistoryDateDivider(
                                    date = item.day,
                                )
                            }
                            is ArchivedRowEntity.Spent -> {
                                ArchivedSpentItem(
                                    transaction = item.transaction,
                                    currency = currency,
                                )
                            }
                            is ArchivedRowEntity.DayTotal -> {
                                TotalPerDay(
                                    spentPerDay = item.total,
                                    currency = currency,
                                )
                            }
                        }
                    }

                    // Empty state for no spends
                    if (spends.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.no_spends),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpentSummaryCard(
    period: Period,
    currency: ExtendCurrency,
) {
    val context = LocalContext.current
    val restBudget = period.budget - period.spent
    val restPercent = if (period.budget > BigDecimal.ZERO) {
        restBudget.multiply(BigDecimal(100)).divide(period.budget, 0, java.math.RoundingMode.HALF_UP)
    } else {
        BigDecimal.ZERO
    }

    val statusColor = when {
        restPercent < BigDecimal.ZERO -> MaterialTheme.colorScheme.error
        restPercent < BigDecimal(20) -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.spent_budget),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = numberFormat(
                            context,
                            period.spent,
                            currency = currency
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.rest_budget),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = numberFormat(
                            context,
                            restBudget,
                            currency = currency
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        color = statusColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (period.spent.toFloat() / period.budget.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
        }
    }
}

private fun buildHistoryList(spends: List<ArchivedTransaction>): List<ArchivedRowEntity> {
    val composedList = mutableListOf<ArchivedRowEntity>()
    var lastSpentDate: LocalDate? = null
    var lastDayTotal: BigDecimal = BigDecimal.ZERO

    spends.forEach { spent ->
        if (lastSpentDate == null || !isSameDay(
                spent.date.time,
                lastSpentDate!!.toDate().time
            )
        ) {
            if (lastSpentDate != null) {
                composedList.add(
                    ArchivedRowEntity.DayTotal(
                        day = lastSpentDate!!,
                        total = lastDayTotal
                    )
                )
            }

            lastSpentDate = spent.date.toLocalDate()
            lastDayTotal = BigDecimal.ZERO

            composedList.add(ArchivedRowEntity.DayDivider(day = lastSpentDate!!))
        }

        lastDayTotal += spent.value
        composedList.add(ArchivedRowEntity.Spent(transaction = spent))
    }

    if (spends.isNotEmpty() && lastSpentDate != null) {
        composedList.add(
            ArchivedRowEntity.DayTotal(
                day = lastSpentDate!!,
                total = lastDayTotal
            )
        )
    }

    return composedList.reversed()
}

@Composable
private fun ArchivedSpentItem(
    transaction: ArchivedTransaction,
    currency: ExtendCurrency,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(Modifier.padding(bottom = 14.dp)) {
        Row(modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .padding(start = 32.dp, top = 14.dp)
                    .weight(1f)
            ) {
                Text(
                    text = numberFormat(context = context, transaction.value, currency = currency),
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                    color = colorOnEditor,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier,
                )
            }
            Box {
                Text(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            start = 16.dp,
                            top = 16.dp,
                            end = 32.dp,
                        ),
                    text = prettyDate(transaction.date, shortMonth = true),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorOnEditor,
                    softWrap = false,
                )
            }
        }
        if (transaction.comment.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 32.dp),
                text = transaction.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = colorOnEditor.copy(alpha = 0.7f),
                softWrap = true,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        ArchiveDetail(periodId = 1)
    }
}
