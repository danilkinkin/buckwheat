package com.luna.dollargrain.home

import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.base.BottomSheetWrapper
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.PathState
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.di.TUTORS
import com.luna.dollargrain.editor.toolbar.DEBUG_MENU_SHEET
import com.luna.dollargrain.editor.toolbar.DebugMenu
import com.luna.dollargrain.editor.toolbar.restBudgetPill.BUDGET_IS_OVER_DESCRIPTION_SHEET
import com.luna.dollargrain.editor.toolbar.restBudgetPill.BudgetIsOverDescription
import com.luna.dollargrain.editor.toolbar.restBudgetPill.NEW_DAY_BUDGET_DESCRIPTION_SHEET
import com.luna.dollargrain.editor.toolbar.restBudgetPill.NewDayBudgetDescription
import com.luna.dollargrain.effects.Confetti
import com.luna.dollargrain.analytics.ANALYTICS_SHEET
import com.luna.dollargrain.analytics.Analytics
import com.luna.dollargrain.analytics.VIEWER_HISTORY_SHEET
import com.luna.dollargrain.analytics.ViewerHistory
import com.luna.dollargrain.onboarding.ON_BOARDING_SHEET
import com.luna.dollargrain.onboarding.Onboarding
import com.luna.dollargrain.recalcBudget.RECALCULATE_DAILY_BUDGET_SHEET
import com.luna.dollargrain.recalcBudget.RecalcBudget
import com.luna.dollargrain.settings.*
import com.luna.dollargrain.wallet.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheets(
    activityResultRegistryOwner: ActivityResultRegistryOwner?,
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
) {
    val isDebug = appViewModel.isDebug.observeAsState(false)
    val coroutineScope = rememberCoroutineScope()

    val requireSetBudget by spendsViewModel.requireSetBudget.observeAsState(false)
    val periodFinished by spendsViewModel.periodFinished.observeAsState(false)

    BottomSheetWrapper(
        name = WALLET_SHEET,
        cancelable = !requireSetBudget && !periodFinished,
    ) { state ->
        Wallet(
            forceChange = periodFinished || requireSetBudget,
            activityResultRegistryOwner = activityResultRegistryOwner,
            onClose = {
                coroutineScope.launch {
                    state.hide()
                }
            }
        )
    }

    BottomSheetWrapper(
        name = DEFAULT_RECALC_BUDGET_CHOOSER,
    ) { state ->
        DefaultRecalcBudgetChooser(
            onClose = {
                coroutineScope.launch {
                    state.hide()
                }
            }
        )
    }

    BottomSheetWrapper(
        name = CURRENCY_EDITOR,
    ) { state ->
        CurrencyEditor(
            onClose = {
                coroutineScope.launch {
                    state.hide()
                }
            }
        )
    }

    BottomSheetWrapper(
        name = FINISH_DATE_SELECTOR_SHEET,
    ) { state ->
        FinishDateSelector(
            selectDate = state.args["initialDate"] as Date?,
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
    ) { state ->
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
        name = ANALYTICS_SHEET,
        cancelable = !periodFinished,
    ) { state ->
        Analytics(
            activityResultRegistryOwner = activityResultRegistryOwner,
            onCreateNewPeriod = {
                appViewModel.openSheet(PathState(WALLET_SHEET))
            },
            onClose = {
                coroutineScope.launch { state.hide() }
            },
        )
    }

    BottomSheetWrapper(name = VIEWER_HISTORY_SHEET) {
        ViewerHistory(
            onClose = {
                coroutineScope.launch { it.hide() }
            }
        )
    }

    BottomSheetWrapper(
        name = ON_BOARDING_SHEET,
        cancelable = false,
    ) { state ->
        Onboarding(
            onSetBudget = {
                appViewModel.openSheet(PathState(WALLET_SHEET))
                appViewModel.activateTutorial(TUTORS.SWIPE_EDIT_SPENT)
            },
            onClose = {
                coroutineScope.launch { state.hide() }
            },
        )
    }

    BottomSheetWrapper(
        name = NEW_DAY_BUDGET_DESCRIPTION_SHEET,
    ) { state ->
        NewDayBudgetDescription(
            onClose = {
                coroutineScope.launch { state.hide() }
            },
        )
    }

    BottomSheetWrapper(
        name = BUDGET_IS_OVER_DESCRIPTION_SHEET,
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
    ) { state ->
        BugReporter(
            onClose = {
                coroutineScope.launch { state.hide() }
            },
        )
    }

    BottomSheetWrapper(
        name = SETTINGS_CHANGE_THEME_SHEET,
    ) { state ->
        ThemeSwitcherDialog(
            onClose = {
                coroutineScope.launch { state.hide() }
            }
        )
    }

    BottomSheetWrapper(
        name = SETTINGS_TRY_WIDGET_SHEET,
    ) { state ->
        TryWidgetDialog()
    }

//    what is this
//    BoxWithConstraints(Modifier.fillMaxSize()) {
//        Confetti(
//            modifier = Modifier.fillMaxSize(),
//            controller = appViewModel.confettiController,
//        )
//    }
}