package com.danilkinkin.buckwheat.finishPeriod

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatTweenSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.WavyShape
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.colorBad
import com.danilkinkin.buckwheat.ui.colorGood
import com.danilkinkin.buckwheat.ui.colorNotGood
import com.danilkinkin.buckwheat.util.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

@Composable
fun RestAndSpentBudgetCard(
    modifier: Modifier = Modifier,
    bigVariant: Boolean = false,
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
) {
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val showSpentCard by appViewModel.showSpentCardByDefault.observeAsState(false)

    val wholeBudget = spendsViewModel.budget.value!!
    val restBudget = spendsViewModel.howMuchBudgetRest().value!!

    val percent = remember { restBudget.divide(wholeBudget, 5, RoundingMode.HALF_EVEN) }

    val overString = stringResource(R.string.over)

    val percentFormatted = remember(showSpentCard) {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        formatter.maximumFractionDigits = 2
        formatter.minimumFractionDigits = 0

        val percentCalculated = if (showSpentCard) {
            BigDecimal(1).minus(percent).multiply(BigDecimal(100))
        } else {
            percent.multiply(BigDecimal(100))
        }

        if (percentCalculated.abs() > BigDecimal(1000)) {
            "$overString ${formatter.format(percentCalculated.coerceIn(BigDecimal(-1000), BigDecimal(1000)))}"
        } else {
            formatter.format(percentCalculated)
        }
    }

    val shift = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        fun anim() {
            coroutineScope.launch {
                shift.animateTo(
                    1f,
                    animationSpec = FloatTweenSpec(if (bigVariant) 6000 else 5000, 0, LinearEasing)
                )
                shift.snapTo(0f)
                anim()
            }
        }

        anim()
    }

    val harmonizedColor = toPalette(
        harmonize(
            combineColors(
                listOf(
                    colorBad,
                    colorNotGood,
                    colorGood,
                ),
                percent.coerceIn(BigDecimal.ZERO, BigDecimal(1)).toFloat(),
            )
        )
    )

    Box(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.extraLarge)
            .clickable { appViewModel.setShowSpentCardByDefault(!showSpentCard) }
    ) {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = harmonizedColor.container,
                contentColor = harmonizedColor.onContainer,
            ),
        ) {
            val textColor = LocalContentColor.current

            Box(
                Modifier
                    .height(IntrinsicSize.Min)
                    .fillMaxWidth()
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                harmonizedColor.main,
                                shape = WavyShape(
                                    period = if (bigVariant) 70.dp else 40.dp,
                                    amplitude = percent.toFloat().clamp(0.96f, 1f) * (if (bigVariant) 3.5.dp else 2.dp),
                                    shift = shift.value,
                                ),
                            )
                            .fillMaxHeight()
                            .fillMaxWidth(percent.toFloat()),
                    )
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(PaddingValues(vertical = 16.dp, horizontal = 24.dp)),
                    horizontalAlignment = if (bigVariant) Alignment.CenterHorizontally else Alignment.Start,
                ) {
                    if (bigVariant) Spacer(modifier = Modifier.height(36.dp))
                    Text(
                        text = numberFormat(
                            if (showSpentCard) {
                                wholeBudget - restBudget
                            } else {
                                restBudget
                            },
                            currency = currency,
                        ),
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = if (bigVariant) MaterialTheme.typography.displaySmall.fontSize else MaterialTheme.typography.titleLarge.fontSize,
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                        lineHeight = TextUnit(0.2f, TextUnitType.Em)
                    )
                    Text(
                        text = if (showSpentCard) {
                            stringResource(R.string.spent_budget)
                        } else {
                            stringResource(R.string.rest_budget)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor.copy(alpha = 0.6f),
                        overflow = TextOverflow.Ellipsis,
                        softWrap = false,
                    )
                    if (bigVariant) {
                        Spacer(modifier = Modifier.height(24.dp))
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    CompositionLocalProvider(
                        LocalContentColor provides textColor,
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(
                                    R.string.rest_budget_percent,
                                    percentFormatted
                                ),
                                style = if (bigVariant) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                                fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            Box(
                Modifier
                    .size(4.dp)
                    .background(
                        color = harmonizedColor.onContainer.copy(alpha = if (showSpentCard) 0.3f else 1f),
                        shape = CircleShape,
                    )
            )
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier
                    .size(4.dp)
                    .background(
                        color = harmonizedColor.onContainer.copy(alpha = if (!showSpentCard) 0.3f else 1f),
                        shape = CircleShape,
                    )
            )
        }
    }
}

@Preview(name = "The budget is almost completely spent")
@Composable
private fun Preview() {
    BuckwheatTheme {
        RestAndSpentBudgetCard()
    }
}

@Preview(name = "Budget half spent")
@Composable
private fun PreviewHalf() {
    BuckwheatTheme {
        RestAndSpentBudgetCard()
    }
}

@Preview(name = "Almost no budget")
@Composable
private fun PreviewFull() {
    BuckwheatTheme {
        RestAndSpentBudgetCard()
    }
}
