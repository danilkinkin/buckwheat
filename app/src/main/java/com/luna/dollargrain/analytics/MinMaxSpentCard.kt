package com.luna.dollargrain.analytics

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.R
import com.luna.dollargrain.data.ExtendCurrency
import com.luna.dollargrain.data.entities.Transaction
import com.luna.dollargrain.data.entities.TransactionType
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.ui.colorMax
import com.luna.dollargrain.ui.colorMin
import com.luna.dollargrain.util.combineColors
import com.luna.dollargrain.util.harmonize
import com.luna.dollargrain.util.isZero
import com.luna.dollargrain.util.numberFormat
import com.luna.dollargrain.util.prettyDate
import com.luna.dollargrain.util.toDate
import com.luna.dollargrain.util.toPalette
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Date

@Composable
fun MinMaxSpentCard(
    modifier: Modifier = Modifier,
    isMin: Boolean,
    spends: List<Transaction>,
    currency: ExtendCurrency,
) {
    val context = LocalContext.current
    
    val minSpent = spends.minByOrNull { it.value }
    val maxSpent = spends.maxByOrNull { it.value }

    val spent = if (isMin) minSpent else maxSpent

    val minValue = minSpent?.value ?: BigDecimal.ZERO
    val maxValue = maxSpent?.value ?: BigDecimal.ZERO
    val currValue = spent?.value ?: BigDecimal.ZERO

    val harmonizedColor = toPalette(
        harmonize(
            combineColors(
                colorMin,
                colorMax,
                if ((maxValue - minValue).isZero()) {
                    if (isMin) 0f else 1f
                } else if (maxValue != BigDecimal.ZERO) {
                    ((currValue - minValue) / (maxValue - minValue)).toFloat()
                } else {
                    0f
                },
            )
        )
    )

    StatCard(
        modifier = modifier,
        value = if (spent != null) {
            numberFormat(
                context,
                spent.value,
                currency = currency,
            )
        } else {
            "-"
        },
        label = if (isMin) "minimum spending" else "maximum spending",
        colors = CardDefaults.cardColors(
            containerColor = harmonizedColor.container,
            contentColor = harmonizedColor.onContainer,
        ),
        content = {
            Spacer(modifier = Modifier.height(6.dp))

            if (spent != null) {
                Text(
                    text = prettyDate(
                        spent.date,
                        showTime = true,
                        forceShowDate = true,
                        shortMonth = true,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )

                if (spent.comment.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Icon(
                            modifier = Modifier.padding(top = 2.dp).size(16.dp),
                            painter = painterResource(R.drawable.ic_label),
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(

                            text = spent.comment,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
        backdropContent = {
            if (spends.isNotEmpty()) {
                SpendsChart(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    spends = spends,
                    markedTransaction = spent,
                    chartPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
                    showBeforeMarked = 4,
                    showAfterMarked = 1,
                )
            }
        }
    )
}

@Preview(name = "Min spent")
@Composable
private fun PreviewMin() {
    DollargrainTheme {
        MinMaxSpentCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            isMin = true,
            currency = ExtendCurrency.none(),
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(56), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(15), date = Date(), comment = "Comment of spent"),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date()),
            ),
        )
    }
}

@Preview(name = "Max spent")
@Composable
private fun PreviewMax() {
    DollargrainTheme {
        MinMaxSpentCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            isMin = false,
            currency = ExtendCurrency.none(),
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(56), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(15), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date()),
            ),
        )
    }
}

@Preview(name = "Min spent (Night mode)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewMinNightMode() {
    DollargrainTheme {
        MinMaxSpentCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            isMin = true,
            currency = ExtendCurrency.none(),
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(56), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(15), date = Date(), comment = "Relly looooooong comment of spent. Nisi ea mollit aute dolore sunt elit veniam"),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date()),
            ),
        )
    }
}

@Preview(name = "Max spent (Night mode)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewMaxNightMode() {
    DollargrainTheme {
        MinMaxSpentCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            isMin = false,
            currency = ExtendCurrency.none(),
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(56), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(15), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date()),
            ),
        )
    }
}

@Preview(name = "Same spends")
@Composable
private fun PreviewWithSameSpends() {
    DollargrainTheme {
        MinMaxSpentCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            isMin = false,
            currency = ExtendCurrency.none(),
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date()),
            ),
        )
    }
}

@Preview(name = "One spent")
@Composable
private fun PreviewWithOneSpent() {
    DollargrainTheme {
        MinMaxSpentCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            isMin = false,
            currency = ExtendCurrency.none(),
            spends = listOf(
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(42),
                    date = Date(),
                    comment = "In id quis ea duis id pariatur exercitation ullamco excepteur id. Aliquip et consectetur adipisicing dolor est dolore veniam excepteur non culpa eu sint aliquip officia. Nulla anim pariatur sit qui ea voluptate anim veniam adipisicing. Minim nostrud ipsum enim pariatur elit culpa. Fugiat voluptate voluptate enim aliqua cupidatat amet magna commodo. Do ad mollit pariatur incididunt exercitation eu laboris et tempor elit cupidatat. Officia sint commodo quis ea ex ut labore irure qui mollit commodo. Labore elit ea nisi eiusmod ut quis minim nostrud ad consectetur incididunt.",
                ),
            ),
        )
    }
}

@Preview(name = "No spends")
@Composable
private fun PreviewWithZeroSpends() {
    DollargrainTheme {
        MinMaxSpentCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            isMin = false,
            currency = ExtendCurrency.none(),
            spends = listOf(),
        )
    }
}