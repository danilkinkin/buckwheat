package com.danilkinkin.buckwheat.finishPeriod

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.isNightMode
import com.danilkinkin.buckwheat.util.HarmonizedColorPalette
import com.danilkinkin.buckwheat.util.harmonizeWithColor
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.toPaletteWithTheme
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date

data class TagUsage(
    val name: String,
    val amount: BigDecimal,
    val color: HarmonizedColorPalette? = null,
)

var colors = listOf(
    Color.Red,
    Color.Green,
    Color.Blue,
    Color.Yellow,
    Color.Magenta,
    Color.Cyan,
    Color.Gray,
    Color.LightGray,
    Color.DarkGray,
    Color.Black,
    Color.White,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoriesChartCard(
    modifier: Modifier = Modifier,
    spends: List<Transaction>,
    currency: ExtendCurrency,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val isNightMode = isNightMode()

    val tags by remember {
        val result = spends
            .groupBy { it.comment }
            .map {
                TagUsage(
                    it.key,
                    it.value.map { it.value }.reduce { acc, next -> acc + next },
                )
            }
            .sortedBy { it.amount }
            .reversed()
            .mapIndexed { index, tagUsage ->
                tagUsage.copy(color = toPaletteWithTheme(
                        color = harmonizeWithColor(
                            designColor = colors[index % colors.size],
                            sourceColor = primaryColor),
                    darkTheme = isNightMode,
                ))
            }
            .toList()

        mutableStateOf(result)
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(),
    ) {
        DonutChart(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                .size(64.dp),
            items = tags,
        )
        FlowRow(
            modifier = Modifier.padding(4.dp, 6.dp),
        ) {
            tags.forEach { tag ->
                Tag(
                    modifier = Modifier.padding(4.dp, 2.dp),
                    value = tag.name,
                    amount = tag.amount,
                    palette = tag.color,
                    currency = currency,
                    onClick = {},
                )
            }
        }
    }
}

@Composable
fun DonutChart(
    modifier: Modifier = Modifier,
    items: List<TagUsage>,
    chartPadding: PaddingValues = PaddingValues(0.dp),
) {
    val localContext = LocalContext.current
    val localDensity = LocalDensity.current

    val layoutDirection = when (LocalConfiguration.current.layoutDirection) {
        0 -> LayoutDirection.Rtl
        1 -> LayoutDirection.Ltr
        else -> LayoutDirection.Rtl
    }
    val topOffset = with(localDensity) { chartPadding.calculateTopPadding().toPx() }
    val bottomOffset = with(localDensity) { chartPadding.calculateBottomPadding().toPx() }
    val startOffset =
        with(localDensity) { chartPadding.calculateStartPadding(layoutDirection).toPx() }
    val endOffset = with(localDensity) { chartPadding.calculateEndPadding(layoutDirection).toPx() }

    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = modifier) {
        val width = this.size.width
        val height = this.size.height
        val heightWithPaddings = height - topOffset - bottomOffset
        val widthWithPaddings = width - startOffset - endOffset

        val total = items.map { it.amount }.reduce { acc, next -> acc + next }
        var offset = 0f

        var gap = 40f
        var halfGap = gap / 2f
        var strokeWidth = 28f
        var halfStrokeWidth = strokeWidth / 2f

        items.forEach { tag ->
            val sweepAngle = tag.amount.divide(total, 5, RoundingMode.HALF_DOWN).multiply(360.toBigDecimal()).toFloat()

            drawArc(
                tag.color?.main ?: Color.Black,
                startAngle = offset + halfGap,
                sweepAngle = sweepAngle - gap,
                useCenter = false,
                topLeft = Offset(startOffset + halfStrokeWidth, topOffset + halfStrokeWidth),
                size = Size(widthWithPaddings - strokeWidth, heightWithPaddings - strokeWidth),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                ),
            )

            offset += sweepAngle
        }
    }
}

@Composable
fun Tag(
    modifier: Modifier = Modifier,
    value: String,
    amount: BigDecimal = BigDecimal.ZERO,
    palette: HarmonizedColorPalette? = null,
    currency: ExtendCurrency,
    onClick: () -> Unit = {},
) {
    val context = LocalContext.current

    Surface(
        shape = CircleShape,
        color = palette?.main ?: MaterialTheme.colorScheme.surface,
        contentColor = palette?.onMain ?: MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .height(44.dp)
            .clip(CircleShape)
            .clickable {
                onClick()
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = value,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = numberFormat(
                    context = context,
                    value = amount,
                    currency = currency,
                ),
                softWrap = false,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.W900),
            )
        }
    }
}

@Preview(name = "Tag chip")
@Composable
private fun Preview() {
    BuckwheatTheme {
        Tag(
            value = "Test tag",
            amount = BigDecimal(123),
            currency = ExtendCurrency.none(),
        )
    }
}

@Preview(name = "3 categories")
@Composable
private fun PreviewMin() {
    BuckwheatTheme {
        CategoriesChartCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            currency = ExtendCurrency.getInstance("EUR"),
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = Date(), comment = "Food"),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = Date(), comment = "Transport"),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date(), comment = "Food"),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = Date(), comment = "Cinema"),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = Date(), comment = "Transport"),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date(), comment = "Food"),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(56), date = Date(), comment = "Entertainment"),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(15), date = Date(), comment = "Food"),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date(), comment = "Transport"),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(56), date = Date(), comment = "Food"),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(15), date = Date(), comment = "Cinema"),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date(), comment = "Transport"),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(120), date = Date(), comment = "Education"),
            ),
        )
    }
}