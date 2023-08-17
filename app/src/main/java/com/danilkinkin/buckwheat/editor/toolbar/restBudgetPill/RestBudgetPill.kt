package com.danilkinkin.buckwheat.editor.toolbar.restBudgetPill

import android.content.res.Configuration.UI_MODE_NIGHT_YES
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.BigIconButton
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.ui.*
import com.danilkinkin.buckwheat.util.*
import com.danilkinkin.buckwheat.wallet.WALLET_SHEET
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
    val percentWithNewSpent by restBudgetPillViewModel.percentWithNewSpent.observeAsState(0f)

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
        Card(
            modifier = Modifier
                .weight(1F)
                .padding(0.dp, 5.dp)
                .height(46.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = harmonizedColor.container.copy(alpha = 0.4f),
                contentColor = harmonizedColor.onContainer,
            ),
            onClick = {
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
