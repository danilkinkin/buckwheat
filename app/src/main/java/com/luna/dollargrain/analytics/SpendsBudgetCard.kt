package com.luna.dollargrain.analytics

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatTweenSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.base.WavyShape
import com.luna.dollargrain.data.ExtendCurrency
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.ui.colorBad
import com.luna.dollargrain.ui.colorGood
import com.luna.dollargrain.ui.colorNotGood
import com.luna.dollargrain.util.combineColors
import com.luna.dollargrain.util.harmonize
import com.luna.dollargrain.util.numberFormat
import com.luna.dollargrain.util.toPalette
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SpendsBudgetCard(
    modifier: Modifier = Modifier,
    budget: BigDecimal,
    spend: BigDecimal,
    currency: ExtendCurrency,
) {
    val context = LocalContext.current
    
    val percent = remember { BigDecimal(1).minus(spend.divide(budget, 2, RoundingMode.HALF_EVEN)) }

    val percentFormatted = remember {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 0

        formatter.format(BigDecimal(1).minus(percent).multiply(BigDecimal(100)))
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
            percent.coerceAtLeast(BigDecimal.ZERO).toFloat(),
        )
    ))

    StatCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = harmonizedColor.container,
            contentColor = harmonizedColor.onContainer,
        ),
        value = numberFormat(
            context,
            spend,
            currency = currency,
        ),
        label = "spent",
        content = {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$percentFormatted% of budget",
                style = MaterialTheme.typography.bodyMedium,
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

@Preview(name = "The budget is almost completely spent")
@Composable
private fun Preview() {
    DollargrainTheme {
        SpendsBudgetCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            spend = BigDecimal(3740),
            budget = BigDecimal(60000),
            currency = ExtendCurrency.none(),
        )
    }
}

@Preview(name = "Budget half spent")
@Composable
private fun PreviewHalf() {
    DollargrainTheme {
        SpendsBudgetCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            spend = BigDecimal(30740),
            budget = BigDecimal(60000),
            currency = ExtendCurrency.none(),
        )
    }
}

@Preview(name = "Almost no budget")
@Composable
private fun PreviewFull() {
    DollargrainTheme {
        SpendsBudgetCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            spend = BigDecimal(45740),
            budget = BigDecimal(60000),
            currency = ExtendCurrency.none(),
        )
    }
}

@Preview(name = "Overspending budget")
@Composable
private fun PreviewOverspending() {
    DollargrainTheme {
        SpendsBudgetCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            spend = BigDecimal.ZERO,
            budget = BigDecimal(60000),
            currency = ExtendCurrency.none(),
        )
    }
}

@Preview(name = "Might mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    DollargrainTheme {
        SpendsBudgetCard(
            modifier = Modifier.height(IntrinsicSize.Min),
            spend = BigDecimal(14740),
            budget = BigDecimal(60000),
            currency = ExtendCurrency.none(),
        )
    }
}