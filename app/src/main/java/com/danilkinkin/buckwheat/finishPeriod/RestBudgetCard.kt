package com.danilkinkin.buckwheat.finishPeriod

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatTweenSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.*
import com.danilkinkin.buckwheat.util.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*
import kotlin.math.ceil

@Composable
fun RestBudgetCard(
    modifier: Modifier = Modifier,
    budget: BigDecimal,
    rest: BigDecimal,
    currency: ExtendCurrency,
) {
    val percent = remember { rest.divide(budget, 5, RoundingMode.HALF_EVEN) }

    val percentFormatted = remember {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 0

        formatter.format(percent.multiply(BigDecimal(100)))
    }

    val shift = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        fun anim() {
            coroutineScope.launch {
                shift.animateTo(
                    1f,
                    animationSpec = FloatTweenSpec(4000, 0, LinearEasing)
                )
                shift.snapTo(0f)
                anim()
            }
        }

        anim()
    }

    val harmonizedColor = toPalette(harmonize(
        combineColors(
            listOf(
                colorBad,
                colorNotGood,
                colorGood,
            ),
            percent.coerceAtLeast(BigDecimal(0)).toFloat(),
        )
    ))

    StatCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = harmonizedColor.container,
            contentColor = harmonizedColor.onContainer,
        ),
        value = prettyCandyCanes(
            rest,
            currency = currency,
        ),
        label = stringResource(R.string.rest_budget),
        content = {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.rest_budget_percent, percentFormatted),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
            )
        },
        backdropContent = {
            Box(
                modifier = Modifier
                    .background(
                        harmonizedColor.main,
                        shape = WavyShape(
                            period = 30.dp,
                            amplitude = 2.dp,
                            shift = shift.value,
                        ),
                    )
                    .fillMaxHeight()
                    .fillMaxWidth(percent.toFloat()),
            )
        }
    )
}

class WavyShape(
    private val period: Dp,
    private val amplitude: Dp,
    private val shift: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ) = Outline.Generic(Path().apply {
        val halfPeriod = with(density) { period.toPx() } / 2
        val amplitude = with(density) { amplitude.toPx() }

        val wavyPath = Path().apply {
            moveTo(x = 0f, y = 0f)
            lineTo(size.width - amplitude, -halfPeriod * 2.5f + halfPeriod * 2 * shift)
            repeat(ceil(size.height / halfPeriod + 3).toInt()) { i ->
                relativeQuadraticBezierTo(
                    dx1 = 2 * amplitude * (if (i % 2 == 0) 1 else -1),
                    dy1 = halfPeriod / 2,
                    dx2 = 0f,
                    dy2 = halfPeriod,
                )
            }
            lineTo(0f, size.height)
        }
        val boundsPath = Path().apply {
            addRect(Rect(offset = Offset.Zero, size = size))
        }
        op(wavyPath, boundsPath, PathOperation.Intersect)
    })
}

@Preview(name = "The budget is almost completely spent")
@Composable
private fun Preview() {
    BuckwheatTheme {
        RestBudgetCard(
            rest = BigDecimal(3740),
            budget = BigDecimal(60000),
            currency = ExtendCurrency(type = CurrencyType.NONE),
        )
    }
}

@Preview(name = "Budget half spent")
@Composable
private fun PreviewHalf() {
    BuckwheatTheme {
        RestBudgetCard(
            rest = BigDecimal(30740),
            budget = BigDecimal(60000),
            currency = ExtendCurrency(type = CurrencyType.NONE),
        )
    }
}

@Preview(name = "Almost no budget")
@Composable
private fun PreviewFull() {
    BuckwheatTheme {
        RestBudgetCard(
            rest = BigDecimal(45740),
            budget = BigDecimal(60000),
            currency = ExtendCurrency(type = CurrencyType.NONE),
        )
    }
}

@Preview(name = "Overspending budget")
@Composable
private fun PreviewOverspending() {
    BuckwheatTheme {
        RestBudgetCard(
            rest = BigDecimal(-3740),
            budget = BigDecimal(60000),
            currency = ExtendCurrency(type = CurrencyType.NONE),
        )
    }
}

@Preview(name = "Might mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        RestBudgetCard(
            rest = BigDecimal(14740),
            budget = BigDecimal(60000),
            currency = ExtendCurrency(type = CurrencyType.NONE),
        )
    }
}