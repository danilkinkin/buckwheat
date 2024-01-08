package com.danilkinkin.buckwheat.finishPeriod.categoriesChart

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.HarmonizedColorPalette
import com.danilkinkin.buckwheat.util.combineColors
import com.danilkinkin.buckwheat.util.harmonize
import com.danilkinkin.buckwheat.util.harmonizeWithColor
import com.danilkinkin.buckwheat.util.toPalette
import java.math.BigDecimal
import java.util.Date

data class TagUsage(
    val name: String,
    val amount: BigDecimal,
    var color: HarmonizedColorPalette? = null,
    var isSpecial: Boolean = false,
)

var baseColors = listOf(
    Color(0xFFF86BAE),
    Color(0xFFF36FFF),
    Color(0xFFAB96FF),
    Color(0xFF5FC7E7),
    Color(0xFF75E584),
    Color(0xFFFFD386),
    Color(0xFFEF7564),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesChartCard(
    modifier: Modifier = Modifier,
    spends: List<Transaction>,
    currency: ExtendCurrency,
) {
    val labelWithoutTag = stringResource(R.string.without_tag)
    val labelRest = stringResource(R.string.rest_tags)
    val maxDisplay = 7

    val colors = baseColors.map {
        toPalette(
            color = harmonizeWithColor(
                designColor = it,
                sourceColor = MaterialTheme.colorScheme.primary
            ),
        )
    }
    val restColor = toPalette(
        color = harmonize(
            designColor = Color(0xFF222222),
            sourceColor = MaterialTheme.colorScheme.primary
        ),
    ).copy(
        onMain = Color(0xFFF4F4F4)
    )

    var offsetColor = 0

    val tags by remember {
        // Convert to TagUsage, group by tag and sum amounts
        var result = spends
            .map { it.copy(comment = it.comment.ifEmpty { labelWithoutTag }) }
            .groupBy { it.comment }
            .map { tag ->
                TagUsage(
                    tag.key,
                    tag.value.map { it.value }.reduce { acc, next -> acc + next },
                    isSpecial = tag.key == labelWithoutTag,
                )
            }
            .sortedBy { it.amount }
            .reversed()
            .toList()

        // Move without tag to the end if list will be overflow
        if (result.size > maxDisplay) {
            result.find { it.name == labelWithoutTag }?.let {
                result = result.filter { tagUsage -> tagUsage.name != labelWithoutTag }
                result = result + it
            }
        }

        // Set colors
        result.subList(0, result.size.coerceAtMost(maxDisplay)).forEachIndexed { index, tagUsage ->
            tagUsage.color = if (tagUsage.name == labelWithoutTag) {
                offsetColor++
                restColor
            } else colors.getOrNull(index - offsetColor) ?: colors.last()
        }

        // Combine rest tags to one
        if (result.size > maxDisplay) {
            result = result.slice(0..<maxDisplay) + TagUsage(
                name = labelRest,
                amount = result
                    .slice(maxDisplay until result.size)
                    .map { it.amount }
                    .reduce { acc, next -> acc + next },
                color = restColor,
                isSpecial = true,
            )
        }

        mutableStateOf(result)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = combineColors(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant,
                angle = 0.3f,
            ),
        )
    ) {
        DonutChart(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                .size(64.dp),
            items = tags,
        )
        FlowRow(Modifier.padding(4.dp, 4.dp)) {
            tags.forEach { tag ->
                TagAmount(
                    modifier = Modifier.padding(4.dp, 4.dp),
                    value = tag.name,
                    amount = tag.amount,
                    palette = tag.color,
                    isSpecial = tag.isSpecial,
                    currency = currency,
                )
            }
        }
    }
}

@Preview(name = "With other")
@Composable
private fun PreviewWithOther() {
    val tags = listOf(
        "Food",
        "Transport",
        "Food",
        "",
        "Cinema",
        "Transport",
        "Food",
        "Entertainment",
        "Food",
        "",
        "Transport",
        "Food",
        "Cinema",
        "Transport",
        "Education"
    )

    BuckwheatTheme {
        CategoriesChartCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            currency = ExtendCurrency.getInstance("EUR"),
            spends = tags.mapIndexed { index, it ->
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(50 + index),
                    date = Date(),
                    comment = it
                )
            },
        )
    }
}

@Preview(name = "Many tags")
@Composable
private fun PreviewManyTags() {
    val tags = listOf(
        "Food",
        "Alcohol",
        "Transport",
        "Plants",
        "Food",
        "Bar",
        "Lost",
        "",
        "Cinema",
        "Transport",
        "Food",
        "Subscriptions",
        "Tools",
        "Entertainment",
        "Food",
        "",
        "Transport",
        "Software",
        "Food",
        "Taxes",
        "Transport",
        "Education"
    )

    BuckwheatTheme {
        CategoriesChartCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            currency = ExtendCurrency.getInstance("EUR"),
            spends = tags.mapIndexed { index, it ->
                Transaction(
                    type = TransactionType.SPENT,
                    value = BigDecimal(50 + index),
                    date = Date(),
                    comment = it
                )
            },
        )
    }
}