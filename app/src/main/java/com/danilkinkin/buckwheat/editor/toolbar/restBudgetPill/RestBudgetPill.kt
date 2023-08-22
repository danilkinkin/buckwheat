package com.danilkinkin.buckwheat.editor.toolbar.restBudgetPill

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.balloon.BalloonScope
import com.danilkinkin.buckwheat.base.BigIconButton
import com.danilkinkin.buckwheat.base.balloon.BalloonState
import com.danilkinkin.buckwheat.base.balloon.rememberBalloonState
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.di.TUTORIAL_STAGE
import com.danilkinkin.buckwheat.di.TUTORS
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.ui.*
import com.danilkinkin.buckwheat.util.*
import com.danilkinkin.buckwheat.wallet.WALLET_SHEET
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.RestBudgetPill(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
    restBudgetPillViewModel: RestBudgetPillViewModel = hiltViewModel(),
) {
    val hideOverspendingWarn by spendsViewModel.hideOverspendingWarn.observeAsState(false)
    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val budgetState by restBudgetPillViewModel.state.observeAsState(DaileBudgetState.NOT_SET)
    val percentWithNewSpent by restBudgetPillViewModel.percentWithNewSpent.observeAsState(1f)
    val tutorial by appViewModel.getTutorialStage(TUTORS.OPEN_WALLET).observeAsState(TUTORIAL_STAGE.NONE)

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
    val percentWithNewSpentAnimated = animateFloatAsState(
        label = "percentWithNewSpentAnimated",
        targetValue = percentWithNewSpent,
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
                percentWithNewSpentAnimated,
            ),
            colorEditor
        )
    )

    if (
        (hideOverspendingWarn && budgetState == DaileBudgetState.BUDGET_END)
        || budgetState == DaileBudgetState.NOT_SET
    ) {
        BigIconButton(
            icon = painterResource(R.drawable.ic_balance_wallet),
            contentDescription = null,
            onClick = { appViewModel.openSheet(PathState(WALLET_SHEET)) },
        )
    } else {
        val balloonState = rememberBalloonState()

        BalloonScope(
            modifier = Modifier.weight(1F),
            balloonState = balloonState,
            content = {
                Text(
                    text = stringResource(R.string.tutorial_open_wallet),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            onClose = {
                appViewModel.passTutorial(TUTORS.OPEN_WALLET)
            }
        ) {
            Card(
                modifier = Modifier
                    .weight(1F)
                    .height(46.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = harmonizedColor.container.copy(alpha = 0.4f),
                    contentColor = harmonizedColor.onContainer,
                ),
                onClick = {
                    balloonState.hide()
                    appViewModel.openSheet(PathState(WALLET_SHEET))
                }
            ) {
                Box(Modifier.fillMaxHeight()) {
                    BackgroundProgress(harmonizedColor)
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        StatusLabel(harmonizedColor)
                        Spacer(modifier = Modifier.weight(1f))
                        ValueLabel(harmonizedColor)
                    }
                }
            }
        }

        DisposableEffect(budgetState) {
            if (tutorial === TUTORIAL_STAGE.READY_TO_SHOW) {
                coroutineScope.launch {
                    delay(2000)
                    balloonState.show()
                }
            }

            onDispose { }
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
