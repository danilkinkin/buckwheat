package com.danilkinkin.buckwheat.editor.toolbar.restBudgetPill

import com.danilkinkin.buckwheat.finishPeriod.StatCard
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatTweenSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.BigIconButton
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.restBudget.BUDGET_IS_OVER_DESCRIPTION_SHEET
import com.danilkinkin.buckwheat.editor.restBudget.NEW_DAY_BUDGET_DESCRIPTION_SHEET
import com.danilkinkin.buckwheat.settings.SETTINGS_SHEET
import com.danilkinkin.buckwheat.ui.*
import com.danilkinkin.buckwheat.util.*
import com.danilkinkin.buckwheat.wallet.WALLET_SHEET
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.RestBudgetPill(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val localDensity = LocalDensity.current

    val overspendingWarnHidden by spendsViewModel.overspendingWarnHidden.observeAsState(false)
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val mode by spendsViewModel.mode.observeAsState(SpendsViewModel.Mode.ADD)

    var restBudgetValue by remember { mutableStateOf(BigDecimal(0)) }
    var restBudgetAbsoluteValue by remember { mutableStateOf(BigDecimal(0)) }
    var budgetPerDaySplit by remember { mutableStateOf("") }
    var finalBudgetValue by remember { mutableStateOf(BigDecimal(0)) }
    var dailyBudget by remember { mutableStateOf(BigDecimal(0)) }
    var editing by remember { mutableStateOf(false) }
    var overdaft by remember { mutableStateOf(false) }
    var endBudget by remember { mutableStateOf(false) }
    var percent by remember { mutableStateOf(BigDecimal(0)) }


    fun calculateValues() {
        if (spendsViewModel.finishDate.value === null) return

        val spentFromDailyBudget = spendsViewModel.spentFromDailyBudget.value!!
        dailyBudget = spendsViewModel.dailyBudget.value!!
        val currentSpent = spendsViewModel.currentSpent

        val newBudget = dailyBudget - spentFromDailyBudget - currentSpent

        overdaft = newBudget < BigDecimal(0)
        restBudgetValue = newBudget
        restBudgetAbsoluteValue = restBudgetValue.coerceAtLeast(BigDecimal(0))

        val newPerDayBudget = spendsViewModel.calcBudgetPerDaySplit(
            applyCurrentSpent = true,
            excludeCurrentDay = true,
        )

        endBudget = newPerDayBudget <= BigDecimal(0)

        budgetPerDaySplit = prettyCandyCanes(
            newPerDayBudget.coerceAtLeast(BigDecimal(0)),
            currency = currency,
        )

        percent = restBudgetValue.divide(dailyBudget, 5, RoundingMode.HALF_EVEN)

        if (restBudgetValue >= 0.toBigDecimal()) {
            finalBudgetValue = restBudgetValue;
        } else {
            finalBudgetValue = newPerDayBudget.coerceAtLeast(BigDecimal(0));
        }
    }

    observeLiveData(spendsViewModel.dailyBudget) {
        calculateValues()
    }

    observeLiveData(spendsViewModel.spentFromDailyBudget) {
        calculateValues()
    }

    observeLiveData(spendsViewModel.stage) {
        editing = it == SpendsViewModel.Stage.EDIT_SPENT

        calculateValues()
    }

    DisposableEffect(currency) {
        calculateValues()

        onDispose { }
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

    val harmonizedColor = toPalette(
        harmonize(
            combineColors(
                listOf(
                    colorBad,
                    colorNotGood,
                    colorGood,
                ),
                percent.coerceAtLeast(BigDecimal(0)).toFloat(),
            )
        )
    )


    if (overspendingWarnHidden && restBudgetValue < 0.toBigDecimal()) {
        BigIconButton(
            icon = painterResource(R.drawable.ic_balance_wallet),
            contentDescription = null,
            onClick = { appViewModel.openSheet(PathState(WALLET_SHEET)) },
        )
    } else {
        Card(
            modifier = Modifier
                .weight(1F)
                .padding(0.dp, 6.dp)
                .height(44.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = harmonizedColor.container,
                contentColor = harmonizedColor.onContainer,
            ),
            onClick = {
                appViewModel.openSheet(PathState(WALLET_SHEET))
            }
        ) {
            val textColor = LocalContentColor.current

            Box(Modifier.fillMaxHeight()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
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

                Row(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    if (restBudgetValue < 0.toBigDecimal()) {
                        Card(
                            modifier = Modifier
                                .height(44.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = CardDefaults.cardColors(
                                containerColor = harmonizedColor.container,
                                contentColor = harmonizedColor.onContainer,
                            ),
                            onClick = {

                                if (endBudget) {
                                    appViewModel.openSheet(PathState(BUDGET_IS_OVER_DESCRIPTION_SHEET))
                                } else {
                                    appViewModel.openSheet(PathState(NEW_DAY_BUDGET_DESCRIPTION_SHEET))
                                }
                            }
                        ) {
                            Row(
                                Modifier.fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Spacer(modifier = Modifier.width(14.dp))
                                Icon(
                                    painter = painterResource(R.drawable.ic_info),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (endBudget) {
                                        stringResource(R.string.budget_end)
                                    } else {
                                        stringResource(R.string.new_daily_budget)
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = textColor.copy(alpha = 0.6f),
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false,
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = stringResource(R.string.rest_budget_for_today),
                            style = MaterialTheme.typography.labelMedium,
                            color = textColor.copy(alpha = 0.6f),
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (!endBudget) {
                        Text(
                            text = prettyCandyCanes(
                                finalBudgetValue,
                                currency = currency,
                            ),
                            style = MaterialTheme.typography.displayLarge,
                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                            lineHeight = TextUnit(0.2f, TextUnitType.Em)
                        )
                    }
                    Spacer(modifier = Modifier.width(22.dp))
                }
            }
        }
    }
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
        Row {
            RestBudgetPill()
            BigIconButton(
                icon = painterResource(R.drawable.ic_settings),
                contentDescription = null,
                onClick = { },
            )
        }
    }
}

@Preview(name = "Budget half spent")
@Composable
private fun PreviewHalf() {
    BuckwheatTheme {
        Row {
            RestBudgetPill()
        }
    }
}

@Preview(name = "Almost no budget")
@Composable
private fun PreviewFull() {
    BuckwheatTheme {
        Row {
            RestBudgetPill()
        }
    }
}

@Preview(name = "Overspending budget")
@Composable
private fun PreviewOverspending() {
    BuckwheatTheme {
        Row {
            RestBudgetPill()
        }
    }
}

@Preview(name = "Might mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        Row {
            RestBudgetPill()
        }
    }
}