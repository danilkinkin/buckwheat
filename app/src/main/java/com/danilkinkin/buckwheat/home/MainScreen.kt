package com.danilkinkin.buckwheat.home

import androidx.activity.result.ActivityResultRegistryOwner
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.base.SwipeableSnackbarHost
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SystemBarState
import com.danilkinkin.buckwheat.editor.EDITOR_SHEET
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.history.History
import com.danilkinkin.buckwheat.settings.CATEGORIES_SHEET
import com.danilkinkin.buckwheat.settings.Settings
import com.danilkinkin.buckwheat.subscriptions.SubscriptionsScreen
import com.danilkinkin.buckwheat.ui.colorBackground
import com.danilkinkin.buckwheat.ui.isNightMode
import com.danilkinkin.buckwheat.util.observeLiveData
import com.danilkinkin.buckwheat.util.setSystemStyle
import com.danilkinkin.buckwheat.wallet.CURRENCY_EDITOR

private enum class MainTab(
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
) {
    HOME(R.string.home, R.drawable.ic_home),
    HISTORY(R.string.history, R.drawable.ic_analytics),
    SUBSCRIPTIONS(R.string.subscriptions_title, R.drawable.ic_balance_wallet),
    SETTINGS(R.string.settings_title, R.drawable.ic_settings),
}

@Composable
fun MainScreen(
    activityResultRegistryOwner: ActivityResultRegistryOwner?,
    appViewModel: AppViewModel = viewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = viewModel(),
) {
    var tab by rememberSaveable { mutableStateOf(MainTab.HOME) }
    val nightMode = remember { mutableStateOf(false) }

    nightMode.value = isNightMode()

    val snackBarMessage = stringResource(R.string.remove_spent)
    val snackBarAction = stringResource(R.string.remove_spent_undo)

    observeLiveData(spendsViewModel.lastRemovedTransaction) {
        appViewModel.showSnackbar(
            message = snackBarMessage,
            actionLabel = snackBarAction,
            duration = SnackbarDuration.Long,
        ) { result ->
            if (result == SnackbarResult.ActionPerformed) {
                spendsViewModel.undoRemoveSpent()
            }
        }
    }

    setSystemStyle(
        style = {
            SystemBarState(
                statusBarDarkBackground = nightMode.value,
                navigationBarDarkBackground = nightMode.value,
            )
        },
        key = nightMode.value,
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(colorBackground),
    ) {
        Scaffold(
            containerColor = colorBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                NavigationBar(containerColor = colorBackground) {
                    MainTab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = {
                                Icon(
                                    painter = painterResource(item.icon),
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(item.label)) },
                        )
                    }
                }
            },
        ) { contentPadding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            ) {
                when (tab) {
                    MainTab.HOME -> HomeScreen(
                        onAdd = {
                            editorViewModel.resetEditingSpent()
                            appViewModel.openSheet(PathState(EDITOR_SHEET))
                        },
                        onOpenSubscriptions = { tab = MainTab.SUBSCRIPTIONS },
                        onOpenHistory = { tab = MainTab.HISTORY },
                        onOpenSettings = { tab = MainTab.SETTINGS },
                    )

                    MainTab.HISTORY -> History(
                        onClose = {
                            appViewModel.openSheet(PathState(EDITOR_SHEET))
                        },
                    )

                    MainTab.SUBSCRIPTIONS -> SubscriptionsScreen()

                    MainTab.SETTINGS -> Box(
                        Modifier
                            .fillMaxSize()
                            .statusBarsPadding(),
                    ) {
                        Settings(
                            onTriedWidget = {},
                            onOpenCurrency = {
                                appViewModel.openSheet(PathState(CURRENCY_EDITOR))
                            },
                            onOpenCategories = {
                                appViewModel.openSheet(PathState(CATEGORIES_SHEET))
                            },
                            onOpenSubscriptions = { tab = MainTab.SUBSCRIPTIONS },
                            activityResultRegistryOwner = activityResultRegistryOwner,
                        )
                    }
                }
            }
        }

        // Sheets are declared last so they draw above the editor overlay.
        BottomSheets(activityResultRegistryOwner)

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 72.dp),
        ) {
            SwipeableSnackbarHost(hostState = remember { appViewModel._snackbarHostState })
        }
    }
}
