package com.luna.dollargrain.history

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.data.ExtendCurrency
import com.luna.dollargrain.data.entities.Transaction
import com.luna.dollargrain.data.entities.TransactionType
import com.luna.dollargrain.ui.BuckwheatTheme
import com.luna.dollargrain.ui.colorOnEditor
import com.luna.dollargrain.util.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Composable
fun SpentItem(
    transaction: Transaction,
    currency: ExtendCurrency,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(Modifier.padding(bottom = 14.dp)) {
        Row(modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .padding( start = 32.dp, top = 14.dp)
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
                modifier = Modifier.padding( horizontal = 32.dp),
                text = transaction.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = colorOnEditor.copy(alpha = 0.7f),
                softWrap = true,
            )
        }
    }
}

@Preview(name = "Default")
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        SpentItem(
            Transaction(
                type = TransactionType.SPENT,
                value = BigDecimal(12340),
                date = Date(),
            ),
            ExtendCurrency.none()
        )
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        SpentItem(
            Transaction(
                type = TransactionType.SPENT,
                value = BigDecimal(12340),
                date = LocalDateTime.now().minusMonths(2).toLocalDate().toDate(),
                comment = "Comment for spent",
            ),
            ExtendCurrency.none()
        )
    }
}

@Preview(name = "With big spent and long comment (Night mode)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewWithBigSpentAndLongCommentNightMode() {
    BuckwheatTheme {
        SpentItem(
            Transaction(
                type = TransactionType.SPENT,
                value = BigDecimal(123456789009876543),
                date = Date(),
                comment = "Very loooong comment for veryyy loooooooooooooooooong spent. And yet row for more length",
            ),
            ExtendCurrency.none()
        )
    }
}

@Preview(name = "Small screen", widthDp = 220)
@Composable
private fun PreviewSmallScreen() {
    BuckwheatTheme {
        SpentItem(
            Transaction(
                type = TransactionType.SPENT,
                value = BigDecimal(12340),
                date = Date(),
            ),
            ExtendCurrency.none()
        )
    }
}
