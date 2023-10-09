package com.danilkinkin.buckwheat.finishPeriod

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatTweenSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.WavyShape
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorBad
import com.danilkinkin.buckwheat.ui.colorGood
import com.danilkinkin.buckwheat.ui.colorNotGood
import com.danilkinkin.buckwheat.util.combineColors
import com.danilkinkin.buckwheat.util.harmonize
import com.danilkinkin.buckwheat.util.numberFormat
import com.danilkinkin.buckwheat.util.toPalette
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
        label = stringResource(R.string.spent_budget),
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

@Preview(name = "The budget is almost completely spent")
@Composable
private fun Preview() {
    BuckwheatTheme {
        SpendsBudgetCard(
            spend = BigDecimal(3740),
            budget = BigDecimal(60000),
            currency = ExtendCurrency.none(),
        )
    }
}

@Preview(name = "Budget half spent")
@Composable
private fun PreviewHalf() {
    BuckwheatTheme {
        SpendsBudgetCard(
            spend = BigDecimal(30740),
            budget = BigDecimal(60000),
            currency = ExtendCurrency.none(),
        )
    }
}

@Preview(name = "Almost no budget")
@Composable
private fun PreviewFull() {
    BuckwheatTheme {
        SpendsBudgetCard(
            spend = BigDecimal(45740),
            budget = BigDecimal(60000),
            currency = ExtendCurrency.none(),
        )
    }
}

@Preview(name = "Overspending budget")
@Composable
private fun PreviewOverspending() {
    BuckwheatTheme {
        SpendsBudgetCard(
            spend = BigDecimal.ZERO,
            budget = BigDecimal(60000),
            currency = ExtendCurrency.none(),
        )
    }
}

@Preview(name = "Might mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        SpendsBudgetCard(
            spend = BigDecimal(14740),
            budget = BigDecimal(60000),
            currency = ExtendCurrency.none(),
        )
    }
}