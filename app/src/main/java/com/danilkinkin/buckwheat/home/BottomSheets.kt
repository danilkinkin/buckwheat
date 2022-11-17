package com.danilkinkin.buckwheat.home

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.base.BottomSheetWrapper
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.*
import com.danilkinkin.buckwheat.editor.restBudget.BUDGET_IS_OVER_DESCRIPTION_SHEET
import com.danilkinkin.buckwheat.editor.restBudget.BudgetIsOverDescription
import com.danilkinkin.buckwheat.editor.restBudget.NEW_DAY_BUDGET_DESCRIPTION_SHEET
import com.danilkinkin.buckwheat.editor.restBudget.NewDayBudgetDescription
import com.danilkinkin.buckwheat.editor.toolbar.DEBUG_MENU_SHEET
import com.danilkinkin.buckwheat.editor.toolbar.DebugMenu
import com.danilkinkin.buckwheat.effects.Confetti
import com.danilkinkin.buckwheat.finishPeriod.FINISH_PERIOD_SHEET
import com.danilkinkin.buckwheat.finishPeriod.FinishPeriod
import com.danilkinkin.buckwheat.onboarding.ON_BOARDING_SHEET
import com.danilkinkin.buckwheat.onboarding.Onboarding
import com.danilkinkin.buckwheat.recalcBudget.RECALCULATE_DAILY_BUDGET_SHEET
import com.danilkinkin.buckwheat.recalcBudget.RecalcBudget
import com.danilkinkin.buckwheat.settings.BUG_REPORTER_SHEET
import com.danilkinkin.buckwheat.settings.BugReporter
import com.danilkinkin.buckwheat.settings.SETTINGS_SHEET
import com.danilkinkin.buckwheat.settings.Settings
import com.danilkinkin.buckwheat.wallet.FINISH_DATE_SELECTOR_SHEET
import com.danilkinkin.buckwheat.wallet.FinishDateSelector
import com.danilkinkin.buckwheat.wallet.WALLET_SHEET
import com.danilkinkin.buckwheat.wallet.Wallet
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheets(
    windowSizeClass: WindowWidthSizeClass,
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
) {
    val isDebug = appViewModel.isDebug.observeAsState(false)
    val coroutineScope = rememberCoroutineScope()

    val requireSetBudget by spendsViewModel.requireSetBudget.observeAsState(false)
    val finishPeriod by spendsViewModel.finishPeriod.observeAsState(false)

    BottomSheetWrapper(
        name = WALLET_SHEET,
        windowSizeClass = windowSizeClass,
        cancelable = !requireSetBudget && !finishPeriod,
    ) { state ->
        Wallet(
            forceChange = finishPeriod || requireSetBudget,
            windowSizeClass = windowSizeClass,
            onClose = {
                coroutineScope.launch {
                    state.hide()
                }
            }
        )
    }

    BottomSheetWrapper(
        name = FINISH_DATE_SELECTOR_SHEET,
        windowSizeClass = windowSizeClass,
    ) { state ->
        FinishDateSelector(
            selectDate = state.args["initialDate"] as Date,
            onBackPressed = {
                coroutineScope.launch {
                    state.hide()
                }
            },
            onApply = {
                coroutineScope.launch {
                    state.hide(mapOf("finishDate" to it))
                }
            },
        )
    }

    BottomSheetWrapper(
        name = SETTINGS_SHEET,
        windowSizeClass = windowSizeClass,
    ) { state ->
        Settings(
            onClose = {
                coroutineScope.launch { state.hide() }
            }
        )
    }

    BottomSheetWrapper(
        name = RECALCULATE_DAILY_BUDGET_SHEET,
        windowSizeClass = windowSizeClass,
        cancelable = false,
    ) { state ->
        RecalcBudget(
            onClose = {
                coroutineScope.launch { state.hide() }
            }
        )
    }

    BottomSheetWrapper(
        name = FINISH_PERIOD_SHEET,
        windowSizeClass = windowSizeClass,
        cancelable = false,
    ) { state ->
        FinishPeriod(
            onCreateNewPeriod = {
                appViewModel.openSheet(PathState(WALLET_SHEET))
            },
            onClose = {
                coroutineScope.launch { state.hide() }
            },
        )
    }

    BottomSheetWrapper(
        name = ON_BOARDING_SHEET,
        windowSizeClass = windowSizeClass,
        cancelable = false,
    ) { state ->
        Onboarding(
            onSetBudget = {
                appViewModel.openSheet(PathState(WALLET_SHEET))
            },
            onClose = {
                coroutineScope.launch { state.hide() }
            },
        )
    }

    BottomSheetWrapper(
        name = NEW_DAY_BUDGET_DESCRIPTION_SHEET,
        windowSizeClass = windowSizeClass,
    ) { state ->
        NewDayBudgetDescription(
            onClose = {
                coroutineScope.launch { state.hide() }
            },
        )
    }

    BottomSheetWrapper(
        name = BUDGET_IS_OVER_DESCRIPTION_SHEET,
        windowSizeClass = windowSizeClass,
    ) { state ->
        BudgetIsOverDescription(
            onClose = {
                coroutineScope.launch { state.hide() }
            },
        )
    }

    if (isDebug.value) {
        BottomSheetWrapper(
            name = DEBUG_MENU_SHEET,
            windowSizeClass = windowSizeClass,
        ) { state ->
            DebugMenu(
                onClose = {
                    coroutineScope.launch { state.hide() }
                },
            )
        }
    }

    BottomSheetWrapper(
        name = BUG_REPORTER_SHEET,
        windowSizeClass = windowSizeClass,
    ) { state ->
        BugReporter(
            onClose = {
                coroutineScope.launch { state.hide() }
            },
        )
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        Confetti(
            modifier = Modifier.fillMaxSize(),
            controller = appViewModel.confettiController,
        )
    }
}