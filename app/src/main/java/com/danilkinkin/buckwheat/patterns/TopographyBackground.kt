package com.danilkinkin.buckwheat.patterns

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.util.smoothPath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Decorative contour lines behind the page header. Zero data semantics: four concentric,
// gently wobbling rings in primary/tertiary that slowly breathe (a 12s drift). Kept at
// alpha 0.04-0.08 so the header text always reads.
@Composable
fun TopographyBackground(modifier: Modifier = Modifier) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
    )
    val transition = rememberInfiniteTransition(label = "topo")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "topoDrift",
    )

    Canvas(modifier = modifier) {
        val radius = size.minDimension
        val center = Offset(size.width / 2f, size.height / 2f)
        val segments = 120
        colors.forEachIndexed { index, color ->
            val inset = index * radius * 0.20f
            val base = radius * 0.18f + inset * 0.55f
            val amplitude = radius * 0.05f
            val points = (0..segments).map { step ->
                val t = step.toFloat() / segments
                val angle = t * 2f * (2 * PI).toFloat()
                val wobble = sin(angle * 2f + drift + index * 1.7f)
                Offset(
                    x = center.x + cos(angle) * base + wobble * amplitude,
                    y = center.y + sin(angle) * base * 0.8f + wobble * amplitude * 0.6f,
                )
            }
            val path = Path().apply { addPath(smoothPath(points)) }
            drawPath(
                path = path,
                color = color.copy(alpha = 0.05f + (index % 2) * 0.03f),
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
    }
}
