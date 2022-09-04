package com.danilkinkin.buckwheat.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import com.danilkinkin.buckwheat.base.ModalBottomSheetValue
import com.danilkinkin.buckwheat.base.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.BottomSheetWrapper
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.editor.Editor
import com.danilkinkin.buckwheat.keyboard.Keyboard
import com.danilkinkin.buckwheat.recalcBudget.RecalcBudget
import com.danilkinkin.buckwheat.settings.Settings
import com.danilkinkin.buckwheat.spendsHistory.BudgetInfo
import com.danilkinkin.buckwheat.spendsHistory.Spent
import com.danilkinkin.buckwheat.topSheet.TopSheetLayout
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.wallet.FinishDateSelector
import com.danilkinkin.buckwheat.wallet.Wallet
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(spendsViewModel: SpendsViewModel = viewModel()) {
    var contentHeight by remember { mutableStateOf(0F) }
    var contentWidth by remember { mutableStateOf(0F) }
    val walletSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val finishDateSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val settingsSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val recalcBudgetSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    val presetFinishDate = remember { mutableStateOf<Date?>(null) }
    val requestFinishDateCallback = remember { mutableStateOf<((finishDate: Date) -> Unit)?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    val localDensity = LocalDensity.current

    val spends = spendsViewModel.getSpends().observeAsState(initial = emptyList())
    val budget = spendsViewModel.budget.observeAsState()
    val startDate = spendsViewModel.startDate
    val finishDate = spendsViewModel.finishDate


    val snackBarMessage = stringResource(R.string.remove_spent)
    val snackBarAction = stringResource(R.string.remove_spent_undo)

    LaunchedEffect(Unit) {
        spendsViewModel.lastRemoveSpent.observe(lifecycleOwner.value) {
            if (it == null) return@observe

            coroutineScope.launch {
                val snackbarResult = snackbarHostState.showSnackbar(
                    message = snackBarMessage,
                    actionLabel = snackBarAction
                )

                if (snackbarResult == SnackbarResult.ActionPerformed) {
                    spendsViewModel.undoRemoveSpent(it)
                }
            }
        }

        spendsViewModel.requireReCalcBudget.observe(lifecycleOwner.value) {
            Log.d("MainScreen", "requireReCalcBudget = $it")
            if (it) {
                coroutineScope.launch {
                    recalcBudgetSheetState.show()
                }
            }
        }

        spendsViewModel.requireSetBudget.observe(lifecycleOwner.value) {
            Log.d("MainScreen", "requireSetBudget = $it")
            if (it) {
                coroutineScope.launch {
                    walletSheetState.show()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                contentWidth = it.size.width.toFloat()
                contentHeight = it.size.height.toFloat()
            },
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = with(localDensity) { (contentHeight - contentWidth).toDp() })
        ) {
            Keyboard(
                modifier = Modifier
                    .height(with(localDensity) { contentWidth.toDp() })
                    .fillMaxWidth()
                    .navigationBarsPadding()
            )
        }

        TopSheetLayout(
            halfHeight = contentHeight - contentWidth,
            itemsCount = spends.value.size + 2,
        ) {
            item {
                BudgetInfo(
                    budget = budget.value ?: BigDecimal(0),
                    startDate = startDate,
                    finishDate = finishDate,
                    currency = spendsViewModel.currency,
                )
                Divider()
            }
            spends.value.forEach {
                item(it.uid) {
                    Spent(
                        spent = it,
                        currency = spendsViewModel.currency,
                        onDelete = {
                            spendsViewModel.removeSpent(it)
                        }
                    )
                    Divider()
                }
            }
            item {
                Editor(
                    modifier = Modifier
                        .fillMaxHeight()
                        .height(with(localDensity) { (contentHeight - contentWidth).toDp() }),
                    onOpenWallet = {
                        coroutineScope.launch {
                            walletSheetState.show()
                        }
                    },
                    onOpenSettings = {
                        coroutineScope.launch {
                            settingsSheetState.show()
                        }
                    },
                    onReaclcBudget = {
                        coroutineScope.launch {
                            recalcBudgetSheetState.show()
                        }
                    },
                )
            }
        }

        BottomSheetWrapper(
            state = walletSheetState,
            cancelable = spendsViewModel.requireSetBudget.value == false,
        ) {
            Wallet(
                requestFinishDate = { presetValue, callback ->
                    coroutineScope.launch {
                        finishDateSheetState.show()

                        presetFinishDate.value = presetValue
                        requestFinishDateCallback.value = callback
                    }
                },
                onClose = {
                    coroutineScope.launch {
                        walletSheetState.hide()
                    }
                }
            )
        }

        BottomSheetWrapper(state = finishDateSheetState) {
            FinishDateSelector(
                selectDate = presetFinishDate.value,
                onBackPressed = {
                    coroutineScope.launch {
                        finishDateSheetState.hide()
                    }
                },
                onApply = {
                    requestFinishDateCallback.value?.let { callback -> callback(it) }
                    coroutineScope.launch {
                        finishDateSheetState.hide()
                    }
                },
            )
        }

        BottomSheetWrapper(state = settingsSheetState) {
            Settings(
                onClose = {
                    coroutineScope.launch {
                        settingsSheetState.hide()
                    }
                }
            )
        }

        /* BottomSheetWrapper(
            state = recalcBudgetSheetState,
            cancelable = false,
        ) {
            RecalcBudget(
                onClose = {
                    coroutineScope.launch {
                        recalcBudgetSheetState.hide()
                    }
                }
            )
        } */
    }
}

@Preview
@Composable
fun MainActivityPreview() {
    BuckwheatTheme {
        MainScreen()
    }
}