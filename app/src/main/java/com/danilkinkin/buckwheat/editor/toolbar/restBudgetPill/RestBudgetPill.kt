package com.danilkinkin.buckwheat.editor.toolbar.restBudgetPill

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.danilkinkin.buckwheat.base.WavyShape
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.ui.*
import com.danilkinkin.buckwheat.util.*
import com.danilkinkin.buckwheat.wallet.WALLET_SHEET
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.RestBudgetPill(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
    restBudgetPillViewModel: RestBudgetPillViewModel = hiltViewModel(),
) {
    val localDensity = LocalDensity.current

    val hideOverspendingWarn by spendsViewModel.hideOverspendingWarn.observeAsState(false)
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())

    val isOverdraft by restBudgetPillViewModel.isOverdraft.observeAsState(false)
    val isBudgetEnd by restBudgetPillViewModel.isBudgetEnd.observeAsState(false)
    val percentWithNewSpent by restBudgetPillViewModel.percentWithNewSpent.observeAsState(0f)
    val percentWithoutNewSpent by restBudgetPillViewModel.percentWithoutNewSpent.observeAsState(0f)
    val todayBudget by restBudgetPillViewModel.todayBudget.observeAsState("")
    val newDailyBudget by restBudgetPillViewModel.newDailyBudget.observeAsState("")

    observeLiveData(spendsViewModel.dailyBudget) {
        restBudgetPillViewModel.calculateValues(editorViewModel.currentSpent)
    }

    observeLiveData(spendsViewModel.spentFromDailyBudget) {
        restBudgetPillViewModel.calculateValues(editorViewModel.currentSpent)
    }

    observeLiveData(editorViewModel.stage) {
        restBudgetPillViewModel.calculateValues(editorViewModel.currentSpent)
    }

    DisposableEffect(currency) {
        restBudgetPillViewModel.calculateValues(editorViewModel.currentSpent)

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
    val percentAnim = animateFloatAsState(
        label = "percentAnim",
        targetValue = percentWithNewSpent.coerceIn(0f, 0.98f),
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

    if (hideOverspendingWarn && isBudgetEnd) {
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
                    if (percentWithoutNewSpent < 0.9999f) {
                        val percentRealAnim by animateFloatAsState(
                            label = "percentRealAnim",
                            targetValue = percentWithoutNewSpent.coerceIn(0f, 0.98f),
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
                    if (percentAnim < 0.9999f) {
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
                                label = "textStartOffset",
                                targetValue = if (isOverdraft) 38.dp else 14.dp,
                                animationSpec = TweenSpec(250),
                            )

                            Spacer(modifier = Modifier.width(textStartOffset))
                            Text(
                                text = if (!isOverdraft && !isBudgetEnd) {
                                    stringResource(R.string.rest_budget_for_today)
                                } else if (isBudgetEnd) {
                                    stringResource(R.string.budget_end)
                                } else {
                                    stringResource(R.string.new_daily_budget_short)
                                },
                                style = MaterialTheme.typography.labelLarge,
                                color = textColor.copy(alpha = 0.6f),
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                        }
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isOverdraft,
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
                                    if (isBudgetEnd) {
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
                        visible = !isBudgetEnd,
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
                            value = if (isOverdraft) newDailyBudget else todayBudget,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                }
            }
        }
    }
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