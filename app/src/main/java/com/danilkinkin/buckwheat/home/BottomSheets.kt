package com.danilkinkin.buckwheat.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.base.BottomSheetWrapper
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.*
import com.danilkinkin.buckwheat.finishPeriod.FINISH_PERIOD_SHEET
import com.danilkinkin.buckwheat.finishPeriod.FinishPeriod
import com.danilkinkin.buckwheat.onboarding.ON_BOARDING_SHEET
import com.danilkinkin.buckwheat.onboarding.Onboarding
import com.danilkinkin.buckwheat.recalcBudget.RECALCULATE_DAILY_BUDGET_SHEET
import com.danilkinkin.buckwheat.recalcBudget.RecalcBudget
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
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
) {
    val isDebug = appViewModel.isDebug.observeAsState(false)
    val coroutineScope = rememberCoroutineScope()

    val requireSetBudget by spendsViewModel.requireSetBudget.observeAsState(false)
    val finishPeriod by spendsViewModel.finishPeriod.observeAsState(false)

    BottomSheetWrapper(
        name = WALLET_SHEET,
        cancelable = !requireSetBudget && !finishPeriod,
    ) { state ->
        Wallet(
            forceChange = finishPeriod || requireSetBudget,
            onClose = {
                coroutineScope.launch {
                    state.hide()
                }
            }
        )
    }

    BottomSheetWrapper(name = FINISH_DATE_SELECTOR_SHEET) { state ->
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

    BottomSheetWrapper(name = SETTINGS_SHEET) { state ->
        Settings(
            onClose = {
                coroutineScope.launch { state.hide() }
            }
        )
    }

    BottomSheetWrapper(
        name = RECALCULATE_DAILY_BUDGET_SHEET,
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

    BottomSheetWrapper(name = NEW_DAY_BUDGET_DESCRIPTION_SHEET) { state ->
        NewDayBudgetDescription(
            onClose = {
                coroutineScope.launch { state.hide() }
            },
        )
    }

    BottomSheetWrapper(name = BUDGET_IS_OVER_DESCRIPTION_SHEET) { state ->
        BudgetIsOverDescription(
            onClose = {
                coroutineScope.launch { state.hide() }
            },
        )
    }

    if (isDebug.value) {
        BottomSheetWrapper(name = DEBUG_MENU_SHEET) { state ->
            DebugMenu(
                onClose = {
                    coroutineScope.launch { state.hide() }
                },
            )
        }
    }
}