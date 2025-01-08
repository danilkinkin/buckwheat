package com.luna.dollargrain.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.data.entities.Transaction
import com.luna.dollargrain.data.entities.TransactionType
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.ui.colorMax
import com.luna.dollargrain.ui.colorMin
import com.luna.dollargrain.ui.isNightMode
import com.luna.dollargrain.util.combineColors
import com.luna.dollargrain.util.harmonize
import com.luna.dollargrain.util.isZero
import com.luna.dollargrain.util.toDate
import com.luna.dollargrain.util.toPalette
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.Date
import kotlin.math.abs

@Composable
fun SpendsChart(
    modifier: Modifier = Modifier,
    spends: List<Transaction>,
    markedTransaction: Transaction? = null,
    showBeforeMarked: Int = spends.size,
    showAfterMarked: Int = spends.size,
    chartPadding: PaddingValues = PaddingValues(0.dp),
) {
    val harmonizeColorMax = if (isNightMode()) {
        toPalette(harmonize(colorMax)).onContainer
    } else {
        toPalette(harmonize(colorMax)).main
    }
    val harmonizeColorMin = if (isNightMode()) {
        toPalette(harmonize(colorMin)).onContainer
    } else {
        toPalette(harmonize(colorMin)).main
    }

    val colors = listOf(
        harmonizeColorMax,
        harmonize(combineColors(harmonizeColorMax, harmonizeColorMin, 0.5f)),
        harmonizeColorMin,
    )
    val minSpentValue = spends.minBy { spent -> spent.value }.value
    val maxSpentValue = spends.maxBy { spent -> spent.value }.value
    val range = maxSpentValue - minSpentValue
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


    val (indexMarked, firstShowIndex, lastShowIndex) = if (markedTransaction != null) {
        val index = spends.indexOfFirst { it.date === markedTransaction.date }

        Triple(
            index,
            (index - showBeforeMarked).coerceAtLeast(0),
            (index + showAfterMarked + 1).coerceAtMost(spends.size),
        )
    } else {
        Triple(null, 0, spends.size)
    }

    Canvas(modifier = modifier) {
        val width = this.size.width
        val height = this.size.height
        val heightWithPaddings = height - topOffset - bottomOffset
        val widthWithPaddings = width - startOffset - endOffset
        val size = (lastShowIndex - firstShowIndex - 1).toFloat().coerceAtLeast(1f)

        val trianglePath = Path().let {
            var lastY = 0f

            spends.subList(firstShowIndex, lastShowIndex).forEachIndexed { index, spent ->
                val scale = if (range.isZero()) {
                    0.5f
                } else {
                    spent.value
                        .minus(minSpentValue)
                        .divide(range, 2, RoundingMode.HALF_EVEN)
                        .toFloat()
                }

                if (index == 0) {
                    lastY = topOffset + heightWithPaddings * (1 - scale)
                    it.moveTo(
                        0f,
                        lastY
                    )
                }

                it.cubicTo(
                    startOffset + widthWithPaddings * ((index - 0.5f).coerceAtLeast(0f) / size),
                    lastY,
                    startOffset + widthWithPaddings * ((index - 0.5f).coerceAtLeast(0f) / size),
                    topOffset + heightWithPaddings * (1 - scale),
                    startOffset + widthWithPaddings * (index / size),
                    topOffset + heightWithPaddings * (1 - scale),
                )

                lastY = topOffset + heightWithPaddings * (1 - scale)
            }


            it.lineTo(width, lastY)

            it.lineTo(width, height)
            it.lineTo(0f, height)

            it
        }


        val chartColors = if (markedTransaction != null) {
            val scale = if (range.isZero()) {
                0.5f
            } else {
                1f - markedTransaction.value
                    .minus(minSpentValue)
                    .divide(range, 2, RoundingMode.HALF_EVEN)
                    .toFloat()
            }

            colors.mapIndexed { index, color ->
                color.copy(alpha = 0.3f - abs(scale - (index / (colors.size - 1))) * 0.25f)
            }
        } else {
            colors.mapIndexed { index, color ->
                color.copy(alpha = 0.3f - (index / (colors.size - 1)) * 0.25f)
            }
        }

        drawPath(
            path = trianglePath,
            Brush.verticalGradient(colors = chartColors),
            style = Fill
        )

        if (markedTransaction != null) {
            val scale = if (range.isZero()) {
                0.5f
            } else {
                markedTransaction.value
                    .minus(minSpentValue)
                    .divide(range, 2, RoundingMode.HALF_EVEN)
                    .toFloat()
            }

            val color = combineColors(
                harmonizeColorMin,
                harmonizeColorMax,
                scale,
            )

            val x = startOffset + widthWithPaddings * ((indexMarked!! - firstShowIndex) / size)
            val y = topOffset + heightWithPaddings * (1 - scale)

            drawCircle(
                color = color.copy(0.2f),
                radius = with(localDensity) { 8.dp.toPx() },
                center = Offset(x, y)
            )

            drawCircle(
                color = color,
                radius = with(localDensity) { 3.dp.toPx() },
                center = Offset(x, y)
            )
        }
    }
}

@Preview
@Composable
private fun PreviewChart() {
    DollargrainTheme {
        val markedTransaction = Transaction(type = TransactionType.SPENT, value = BigDecimal(30), date = Date())

        SpendsChart(
            modifier = Modifier.size(100.dp),
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(2).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(52), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(72), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = LocalDate.now().minusDays(1).toDate()),
                markedTransaction,
                Transaction(type = TransactionType.SPENT, value = BigDecimal(15), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(42), date = Date()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(62), date = Date()),
            ),
            markedTransaction = markedTransaction,
            chartPadding = PaddingValues(vertical = 16.dp)
        )
    }
}

@Preview
@Composable
private fun PreviewChartWithSameSpends() {
    DollargrainTheme {
        val markedTransaction = Transaction(type = TransactionType.SPENT, value = BigDecimal(30), date = Date())

        SpendsChart(
            modifier = Modifier.size(100.dp),
            spends = listOf(
                Transaction(type = TransactionType.SPENT, value = BigDecimal(30), date = LocalDate.now().minusDays(1).toDate()),
                Transaction(type = TransactionType.SPENT, value = BigDecimal(30), date = LocalDate.now().minusDays(1).toDate()),
                markedTransaction,
                Transaction(type = TransactionType.SPENT, value = BigDecimal(30), date = Date()),
            ),
            markedTransaction = markedTransaction,
            chartPadding = PaddingValues(vertical = 16.dp)
        )
    }
}

@Preview
@Composable
private fun PreviewChartWithOneSpent() {
    DollargrainTheme {
        val markedTransaction = Transaction(type = TransactionType.SPENT, value = BigDecimal(30), date = Date())

        SpendsChart(
            modifier = Modifier.size(100.dp),
            spends = listOf(
                markedTransaction,
            ),
            markedTransaction = markedTransaction,
            chartPadding = PaddingValues(vertical = 16.dp)
        )
    }
}