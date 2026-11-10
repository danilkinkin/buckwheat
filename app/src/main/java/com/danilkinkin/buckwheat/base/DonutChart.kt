package com.danilkinkin.buckwheat.base

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

data class DonutSegment(
    val value: Float,
    val color: Color,
)

/**
 * A quiet ring chart. Segments grow in once over 550ms; no spring, no looping motion.
 */
@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 18.dp,
    center: @Composable () -> Unit,
) {
    val total = segments.sumOf { it.value.toDouble() }.toFloat()
    val progress = remember { Animatable(0f) }

    LaunchedEffect(segments) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        )
    }

    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val gap = if (segments.size > 1) 2.5f else 0f

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val radius = min(size.width, size.height) / 2f - stroke / 2f
            val arcSize = Size(radius * 2f, radius * 2f)
            val topLeft = Offset(
                x = (size.width - arcSize.width) / 2f,
                y = (size.height - arcSize.height) / 2f,
            )

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )

            if (total <= 0f) return@Canvas

            var startAngle = -90f
            segments.forEach { segment ->
                val sweep = (segment.value / total) * 360f
                if (sweep > 0f) {
                    drawArc(
                        color = segment.color,
                        startAngle = startAngle + gap / 2f,
                        sweepAngle = ((sweep - gap).coerceAtLeast(0.5f)) * progress.value,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                }
                startAngle += sweep
            }
        }

        center()
    }
}
