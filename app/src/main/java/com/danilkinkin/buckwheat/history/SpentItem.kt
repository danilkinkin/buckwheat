package com.danilkinkin.buckwheat.history

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.SubscriptionService
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.subscriptions.ServiceBadge
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorOnEditor
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toDate
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Date

/**
 * One ledger row: category badge, note (or category when there is no note), time, amount.
 * The amount moved from display type to a trailing value so a list is scannable; the day header
 * carries the date and day total instead of repeating them per row.
 */
@Composable
fun SpentItem(
    transaction: Transaction,
    currency: ExtendCurrency,
    modifier: Modifier = Modifier,
    category: Category? = null,
    service: SubscriptionService? = null,
    showDate: Boolean = false,
) {
    val context = LocalContext.current
    val note = transaction.comment.trim()
    val time = if (showDate) {
        // Home shows spends from several days; History relies on its day dividers instead.
        prettyDate(transaction.date, human = true, shortMonth = true)
    } else {
        prettyDate(transaction.date, forceHideDate = true)
    }

    val primary = note.ifBlank {
        category?.name ?: stringResource(R.string.no_category)
    }
    val secondary = buildList {
        if (note.isNotBlank() && category?.name != null) add(category.name)
        add(time)
    }.joinToString(" · ")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (service != null) {
            ServiceBadge(service = service, size = 44.dp)
        } else {
            val badgeColor = category?.let {
                Color(it.color).copy(alpha = 0.22f)
            } ?: MaterialTheme.colorScheme.surfaceContainerHighest

            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = badgeColor,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (!category?.emoji.isNullOrEmpty()) {
                        Text(
                            text = category.emoji,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.ic_label),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = primary,
                style = MaterialTheme.typography.bodyLarge,
                color = colorOnEditor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = secondary,
                style = MaterialTheme.typography.bodySmall,
                color = colorOnEditor.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            text = numberFormat(context = context, transaction.value, currency = currency),
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
            color = colorOnEditor,
            maxLines = 1,
            softWrap = false,
        )
    }
}

private val previewCategory = Category(
    id = 1,
    name = "Food",
    emoji = "🍜",
    color = 0xFFFF8A65,
)

@Preview(name = "Default")
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        SpentItem(
            Transaction(
                type = TransactionType.SPENT,
                value = BigDecimal(12340),
                date = Date(),
                comment = "Lunch with the team",
            ),
            ExtendCurrency.none(),
            category = previewCategory,
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
                comment = "",
            ),
            ExtendCurrency.none(),
        )
    }
}

@Preview(name = "Long text and big value (Night mode)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLongText() {
    BuckwheatTheme {
        SpentItem(
            Transaction(
                type = TransactionType.SPENT,
                value = BigDecimal(123456789009876543),
                date = Date(),
                comment = "Very loooong note for a veryyyyy loooooooooooooooooong spend.",
            ),
            ExtendCurrency.none(),
            category = previewCategory.copy(name = "Transport", emoji = "🚌"),
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
            ExtendCurrency.none(),
        )
    }
}
