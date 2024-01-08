package com.danilkinkin.buckwheat.finishPeriod.categoriesChart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import java.math.RoundingMode

@Composable
fun DonutChart(
    modifier: Modifier = Modifier,
    items: List<TagUsage>,
    chartPadding: PaddingValues = PaddingValues(0.dp),
) {
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

    Canvas(modifier = modifier) {
        val width = this.size.width
        val height = this.size.height
        val heightWithPaddings = height - topOffset - bottomOffset
        val widthWithPaddings = width - startOffset - endOffset

        val total = items.map { it.amount }.reduce { acc, next -> acc + next }
        var offset = 0f

        val gap = 0f
        val halfGap = gap / 2f
        val strokeWidth = 28f
        val halfStrokeWidth = strokeWidth / 2f
        val minSweepAngle = 28f
        val offsetAngle = -90f

        var itemAngles = items.map {
            it.amount
                .divide(total, 5, RoundingMode.HALF_DOWN)
                .multiply(360.toBigDecimal())
                .toFloat()
        }

        val shareAngle = itemAngles
            .filter { it < minSweepAngle }
            .map { minSweepAngle - it }
            .fold(0f) { acc, next -> acc + next }
        val splitItems = itemAngles.filter { it > minSweepAngle }.toMutableList()

        itemAngles = itemAngles.map { angle ->
            if (angle < minSweepAngle) {
                return@map minSweepAngle
            }

            if (angle > minSweepAngle) {
                return@map angle - shareAngle / splitItems.size
            }

            angle
        }

        items.forEachIndexed { index, tag ->
            val sweepAngle = itemAngles[index]

            drawArc(
                tag.color?.main ?: Color.Black,
                startAngle = offset + halfGap + offsetAngle,
                sweepAngle = sweepAngle - gap,
                useCenter = false,
                topLeft = Offset(startOffset + halfStrokeWidth, topOffset + halfStrokeWidth),
                size = Size(widthWithPaddings - strokeWidth, heightWithPaddings - strokeWidth),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Butt
                ),
            )

            offset += sweepAngle
        }
    }
}