package com.danilkinkin.buckwheat.editor.toolbar.restBudgetPill

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.danilkinkin.buckwheat.base.AnimatedNumber
import com.danilkinkin.buckwheat.base.BigIconButton
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.*
import com.danilkinkin.buckwheat.util.*
import com.danilkinkin.buckwheat.wallet.WALLET_SHEET
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun RowScope.RestBudgetPill(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val localDensity = LocalDensity.current

    val overspendingWarnHidden by spendsViewModel.overspendingWarnHidden.observeAsState(false)
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())

    var restBudgetValue by remember { mutableStateOf(BigDecimal(0)) }
    var restBudgetAbsoluteValue by remember { mutableStateOf(BigDecimal(0)) }
    var budgetPerDaySplit by remember { mutableStateOf("") }
    var previewBudgetValue by remember { mutableStateOf(BigDecimal(0)) }
    var finalBudgetValue by remember { mutableStateOf(BigDecimal(0)) }
    var dailyBudget by remember { mutableStateOf(BigDecimal(0)) }
    var editing by remember { mutableStateOf(false) }
    var overdaft by remember { mutableStateOf(false) }
    var endBudget by remember { mutableStateOf(false) }
    var percent by remember { mutableStateOf(BigDecimal(0)) }
    var percentReal by remember { mutableStateOf(BigDecimal(0)) }
    var dataIsLoading by remember { mutableStateOf(true) }


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

        percent = if (dailyBudget > BigDecimal(0)) restBudgetValue.divide(
            dailyBudget,
            5,
            RoundingMode.HALF_EVEN
        ) else BigDecimal(0)
        percentReal = if (dailyBudget > BigDecimal(0)) (dailyBudget - spentFromDailyBudget).divide(
            dailyBudget,
            5,
            RoundingMode.HALF_EVEN
        ) else BigDecimal(0)

        previewBudgetValue = finalBudgetValue
        finalBudgetValue = if (restBudgetValue >= 0.toBigDecimal()) {
            restBudgetValue
        } else {
            newPerDayBudget.coerceAtLeast(BigDecimal(0))
        }

        dataIsLoading = false
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
    val percentAnim = if (dataIsLoading) 1f else animateFloatAsState(
        targetValue = percent.toFloat().coerceAtLeast(0f).coerceIn(0f, 0.98f),
        animationSpec = TweenSpec(300),
    ).value

    val harmonizedColor = toPalette(
        harmonize(
            combineColors(
                listOf(
                    colorBad,
                    colorNotGood,
                    colorGood,
                ),
                percentAnim,
            ),
            colorEditor
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
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = harmonizedColor.container.copy(alpha = 0.4f),
                contentColor = harmonizedColor.onContainer,
            ),
            onClick = {
                appViewModel.openSheet(PathState(WALLET_SHEET))
            }
        ) {
            val textColor = LocalContentColor.current

            Box(Modifier.fillMaxHeight()) {
                Box(Modifier.fillMaxSize()) {
                    if (percentReal.toFloat() < 0.9999f) {
                        val percentRealAnim by animateFloatAsState(
                            targetValue = percentReal.toFloat().coerceIn(0f, 0.98f),
                            animationSpec = TweenSpec(250),
                        )

                        Box(
                            modifier = Modifier
                                .background(
                                    harmonizedColor.main.copy(alpha = 0.1f),
                                    shape = WavyShape(
                                        period = 30.dp,
                                        amplitude = 2.dp,
                                        shift = shift.value,
                                    ),
                                )
                                .fillMaxHeight()
                                .fillMaxWidth(percentRealAnim),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .background(harmonizedColor.main.copy(alpha = 0.1f))
                                .fillMaxSize(),
                        )
                    }
                }

                Box(Modifier.fillMaxSize()) {
                    if (percent.toFloat() < 0.9999f) {
                        Box(
                            modifier = Modifier
                                .background(
                                    harmonizedColor.main.copy(alpha = 0.15f),
                                    shape = WavyShape(
                                        period = 30.dp,
                                        amplitude = 2.dp,
                                        shift = shift.value,
                                    ),
                                )
                                .fillMaxHeight()
                                .fillMaxWidth(percentAnim),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .background(harmonizedColor.main.copy(alpha = 0.15f))
                                .fillMaxSize(),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Box(contentAlignment = Alignment.CenterStart) {
                        Row(
                            modifier = Modifier.height(44.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val textStartOffset by animateDpAsState(
                                targetValue = if (restBudgetValue < 0.toBigDecimal()) 38.dp else 14.dp,
                                animationSpec = TweenSpec(250),
                            )

                            Spacer(modifier = Modifier.width(textStartOffset))
                            Text(
                                text = if (restBudgetValue >= 0.toBigDecimal()) {
                                    stringResource(R.string.rest_budget_for_today)
                                } else if (endBudget) {
                                    stringResource(R.string.budget_end)
                                } else {
                                    stringResource(R.string.new_daily_budget_short)
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = textColor.copy(alpha = 0.6f),
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                        }
                        androidx.compose.animation.AnimatedVisibility(
                            visible = restBudgetValue < 0.toBigDecimal(),
                            enter = fadeIn(tween(durationMillis = 250)),
                            exit = fadeOut(tween(durationMillis = 250)),
                        ) {
                            Card(
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = harmonizedColor.container.copy(alpha = 0f),
                                    contentColor = harmonizedColor.onContainer,
                                ),
                                onClick = {
                                    if (endBudget) {
                                        appViewModel.openSheet(
                                            PathState(
                                                BUDGET_IS_OVER_DESCRIPTION_SHEET
                                            )
                                        )
                                    } else {
                                        appViewModel.openSheet(
                                            PathState(
                                                NEW_DAY_BUDGET_DESCRIPTION_SHEET
                                            )
                                        )
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
                                    Spacer(modifier = Modifier.width(14.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    AnimatedVisibility(
                        visible = !endBudget,
                        enter = fadeIn(
                            tween(
                                durationMillis = 150,
                                easing = EaseInOutQuad,
                            )
                        ) + slideInHorizontally(
                            tween(
                                durationMillis = 150,
                                easing = EaseInOutQuad,
                            )
                        ) { with(localDensity) { 20.dp.toPx().toInt() } },
                        exit = fadeOut(
                            tween(
                                durationMillis = 150,
                                easing = EaseInOutQuad,
                            )
                        ) + slideOutHorizontally(
                            tween(
                                durationMillis = 150,
                                easing = EaseInOutQuad,
                            )
                        ) { with(localDensity) { 20.dp.toPx().toInt() } },
                    ) {
                        AnimatedNumber(
                            value = prettyCandyCanes(
                                if (!endBudget) finalBudgetValue else previewBudgetValue,
                                currency = currency,
                            ),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = MaterialTheme.typography.titleLarge.fontSize
                            ),
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