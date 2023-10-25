package com.danilkinkin.buckwheat.finishPeriod

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorBad
import com.danilkinkin.buckwheat.ui.colorGood
import com.danilkinkin.buckwheat.ui.colorNotGood
import com.danilkinkin.buckwheat.util.countDays
import com.danilkinkin.buckwheat.util.harmonize
import com.danilkinkin.buckwheat.util.isSameDay
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toPalette
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.Date

@Composable
fun OverspendingInfoCard(
    modifier: Modifier = Modifier,
    budget: BigDecimal,
    spends: List<Transaction>,
    startDate: Date,
    finishDate: Date,
    currency: ExtendCurrency,
) {
    val context = LocalContext.current
    
    val days = countDays(finishDate, startDate)
    val spentPerDay = (budget / days.toBigDecimal()).setScale(0, RoundingMode.FLOOR)

    val overspendingDays = remember(spends) {
        var overspendingDays: MutableList<OverspendingDay> = emptyList<OverspendingDay>().toMutableList()
        var currOverspendingDay: OverspendingDay? = null

        spends.forEach {
            if (
                currOverspendingDay == null ||
                !isSameDay(currOverspendingDay!!.date.time, it.date.time)
            ) {
                if (currOverspendingDay !== null) {
                    overspendingDays.add(currOverspendingDay!!)
                }

                currOverspendingDay = OverspendingDay(
                    date = it.date,
                    spends = listOf(it),
                    spending = it.value,
                )

                return@forEach
            }

            currOverspendingDay = currOverspendingDay!!.copy(
                spending = currOverspendingDay!!.spending + it.value,
                spends = currOverspendingDay!!.spends.plus(it)
            )
        }

        if (currOverspendingDay != null) {
            overspendingDays.add(currOverspendingDay!!)
        }

        overspendingDays.filter { it.spending > spentPerDay }.toMutableList()
    }

    if (overspendingDays.isEmpty()) {
        val harmonizedColor = toPalette(harmonize(colorGood))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = harmonizedColor.container,
                contentColor = harmonizedColor.onContainer,
            )
        ) {
            Column(Modifier.padding(vertical = 32.dp, horizontal = 24.dp)) {
                Text(
                    text = stringResource(R.string.overspending_never_exceeded_daily_budget),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                    softWrap = true,
                )
            }
        }
    } else {
        val restDays = countDays(finishDate, overspendingDays.first().date)

        val harmonizedColor = when {
            overspendingDays.size == 1 -> toPalette(harmonize(colorGood))
            overspendingDays.size <= restDays * 0.4f -> toPalette(harmonize(colorNotGood))
            else -> toPalette(harmonize(colorBad))
        }

        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = harmonizedColor.surfaceVariant,
                contentColor = harmonizedColor.onSurfaceVariant,
            ),
        ) {
            val textColor = LocalContentColor.current

            Column {
                Column(Modifier.padding(vertical = 16.dp, horizontal = 24.dp)) {
                    Text(
                        text = prettyDate(
                            overspendingDays.first().date,
                            showTime = false,
                            forceShowDate = true,
                        ),
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        lineHeight = TextUnit(0.2f, TextUnitType.Em)
                    )
                    Text(
                        text = stringResource(
                            R.string.overspending_value,
                            numberFormat(
                                context,
                                overspendingDays.first().spending - spentPerDay,
                                currency = currency,
                            ),
                        ),
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        lineHeight = TextUnit(0.2f, TextUnitType.Em)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.overspending_first_time),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                        color = textColor.copy(alpha = 0.6f),
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = harmonizedColor.container.copy(),
                        contentColor = harmonizedColor.onContainer,
                    )
                ) {
                    Column(Modifier.padding(vertical = 32.dp, horizontal = 24.dp)) {
                        val string = when {
                            overspendingDays.size == 1 -> {
                                stringResource(R.string.overspending_after_not_go_out_budget)
                            }
                            overspendingDays.size <= restDays * 0.4f -> {
                                String.format(
                                    stringResource(R.string.overspending_after_few_times_out_budget),
                                    String.format(
                                        pluralStringResource(R.plurals.days_count, count = overspendingDays.size - 1),
                                        overspendingDays.size - 1,
                                    ),
                                    restDays,
                                )
                            }
                            restDays - overspendingDays.size == 0 -> {
                                String.format(
                                    stringResource(R.string.overspending_after_all_times_out_budget),
                                    restDays,
                                )
                            }
                            else -> {
                                String.format(
                                    stringResource(R.string.overspending_after_many_times_out_budget),
                                    String.format(
                                        pluralStringResource(R.plurals.days_count, count = restDays - overspendingDays.size),
                                        restDays - overspendingDays.size,
                                    ),
                                    restDays,
                                )
                            }
                        }

                        Text(
                            text = string,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = MaterialTheme.typography.labelLarge.fontSize,
                            softWrap = true,
                        )
                    }
                }
            }
        }
    }
}

data class OverspendingDay(
    val date: Date,
    val spends: List<Transaction>,
    val spending: BigDecimal,
)

@Preview(name = "Zero overspending")
@Composable
private fun PreviewZero() {
    BuckwheatTheme {
        OverspendingInfoCard(
            budget = BigDecimal(200),
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(3), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(5), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(8), date = Date()),
            ),
            currency = ExtendCurrency.none(),
            startDate = LocalDate.now().minusDays(7).toDate(),
            finishDate = LocalDate.now().plusDays(3).toDate(),
        )
    }
}

@Preview(name = "One overspending")
@Composable
private fun PreviewOne() {
    BuckwheatTheme {
        OverspendingInfoCard(
            budget = BigDecimal(200),
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(30), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(15), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date()),
            ),
            currency = ExtendCurrency.none(),
            startDate = LocalDate.now().minusDays(7).toDate(),
            finishDate = LocalDate.now().plusDays(3).toDate(),
        )
    }
}

@Preview(name = "Few overspending")
@Composable
private fun PreviewFew() {
    BuckwheatTheme {
        OverspendingInfoCard(
            budget = BigDecimal(200),
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(5).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(5).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(5).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(30), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(15), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date()),
            ),
            currency = ExtendCurrency.none(),
            startDate = LocalDate.now().minusDays(10).toDate(),
            finishDate = Date(),
        )
    }
}

@Preview(name = "Many overspending")
@Composable
private fun PreviewMany() {
    BuckwheatTheme {
        OverspendingInfoCard(
            budget = BigDecimal(200),
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(4).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(4).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(4).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(3).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(3).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(3).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(30), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(15), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date()),
            ),
            currency = ExtendCurrency.none(),
            startDate = LocalDate.now().minusDays(7).toDate(),
            finishDate = LocalDate.now().plusDays(3).toDate(),
        )
    }
}

@Preview(name = "All overspending")
@Composable
private fun PreviewAll() {
    BuckwheatTheme {
        OverspendingInfoCard(
            budget = BigDecimal(200),
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(30), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(15), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date()),
            ),
            currency = ExtendCurrency.none(),
            startDate = LocalDate.now().minusDays(9).toDate(),
            finishDate = LocalDate.now().plusDays(1).toDate(),
        )
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        OverspendingInfoCard(
            budget = BigDecimal(200),
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(30), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(15), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date()),
            ),
            currency = ExtendCurrency.none(),
            startDate = LocalDate.now().minusDays(7).toDate(),
            finishDate = LocalDate.now().plusDays(3).toDate(),
        )
    }
}