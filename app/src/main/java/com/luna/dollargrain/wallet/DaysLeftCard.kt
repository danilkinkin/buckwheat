package com.luna.dollargrain.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.util.countDays
import com.luna.dollargrain.util.countDaysToToday
import com.luna.dollargrain.util.toDate
import java.time.LocalDate
import java.util.Date

@Composable
fun DaysLeftCard(
    modifier: Modifier = Modifier,
    startDate: Date,
    finishDate: Date?,
    colors: CardColors = CardDefaults.cardColors(),
) {
    val localDensity = LocalDensity.current

    var size by remember { mutableStateOf(0.dp) }

    val days = countDays(finishDate!!, startDate)
    val restDays = countDaysToToday(finishDate)

    Box(
        Modifier
            .widthIn(max = 120.dp)
            .fillMaxHeight()
            .onGloballyPositioned {
                size = with(localDensity) { it.size.height.toDp() }
            }
    ) {
        Card(
            modifier = modifier
                .size(size)
                .clip(CircleShape),
            shape = CircleShape,
            colors = colors
        ) {
            val textColor = LocalContentColor.current

            Box(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary,
                            shape = ArcShape(
                                thickness = 6.dp,
                                progress = restDays / days.toFloat(),
                            ),
                        )
                        .fillMaxHeight()
                        .fillMaxWidth(),
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = restDays.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = MaterialTheme.typography.titleLarge.fontSize
                        )
                    )
                    Text(
                        text = "days left",
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.6f),
                    )
                    Spacer(modifier = Modifier.height(size * 0.1f))
                }
            }
        }
    }
}

class ArcShape(
    private val thickness: Dp,
    private val progress: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ) = Outline.Generic(Path().apply {
        val fixedProgress = progress - 0.000001f
        val thicknessPx = with(density) { thickness.toPx() }
        val shift = -90f

        val wavyPath = Path().apply {
            arcTo(
                Rect(offset = Offset.Zero, size = size),
                shift,
                -360 * fixedProgress,
                forceMoveTo = true,
            )
            arcTo(
                Rect(
                    offset = Offset(thicknessPx, thicknessPx),
                    size = Size(width = size.width - thicknessPx * 2, height = size.height - thicknessPx * 2),
                ),
                -360 * fixedProgress + shift,
                360 * fixedProgress,
                forceMoveTo = false,
            )
        }
        val boundsPath = Path().apply {
            addRect(Rect(offset = Offset.Zero, size = size))
        }
        op(wavyPath, boundsPath, PathOperation.Intersect)
    })
}

@Preview
@Composable
private fun Preview() {
    DollargrainTheme {
        Box(
            modifier = Modifier
                .height(200.dp)
                .width(900.dp)
        ) {
            DaysLeftCard(
                startDate = LocalDate.now().minusDays(18).toDate(),
                finishDate = LocalDate.now().plusDays(10).toDate(),
            )
        }
    }
}