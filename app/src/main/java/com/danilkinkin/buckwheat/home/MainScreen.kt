package com.danilkinkin.buckwheat.home

import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
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
import com.danilkinkin.buckwheat.base.SwipeableSnackbarHost
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.data.SystemBarState
import com.danilkinkin.buckwheat.editor.EDITOR_SHEET
import com.danilkinkin.buckwheat.editor.EditorViewModel
import com.danilkinkin.buckwheat.history.History
import com.danilkinkin.buckwheat.settings.CATEGORIES_SHEET
import com.danilkinkin.buckwheat.settings.Settings
import com.danilkinkin.buckwheat.stats.StatsScreen
import com.danilkinkin.buckwheat.subscriptions.SubscriptionsScreen
import com.danilkinkin.buckwheat.ui.colorBackground
import com.danilkinkin.buckwheat.ui.isNightMode
import com.danilkinkin.buckwheat.util.observeLiveData
import com.danilkinkin.buckwheat.util.setSystemStyle
import com.danilkinkin.buckwheat.wallet.CURRENCY_EDITOR

private enum class AppScreen { HOME, HISTORY, SUBSCRIPTIONS, STATS, SETTINGS }

@Composable
fun MainScreen(
    activityResultRegistryOwner: ActivityResultRegistryOwner?,
    appViewModel: AppViewModel = viewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = viewModel(),
) {
    var screen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
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

    BackHandler(enabled = screen != AppScreen.HOME) {
        screen = AppScreen.HOME
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorBackground,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(Modifier.fillMaxSize()) {
            when (screen) {
                AppScreen.HOME -> HomeScreen(
                    onAdd = {
                        editorViewModel.resetEditingSpent()
                        appViewModel.openSheet(PathState(EDITOR_SHEET, mapOf("tab" to "expense")))
                    },
                    onOpenSubscriptions = { screen = AppScreen.SUBSCRIPTIONS },
                    onOpenHistory = { screen = AppScreen.HISTORY },
                    onOpenStats = { screen = AppScreen.STATS },
                    onOpenSettings = { screen = AppScreen.SETTINGS },
                )

                AppScreen.HISTORY -> History(
                    onBack = { screen = AppScreen.HOME },
                    onClose = {
                        appViewModel.openSheet(PathState(EDITOR_SHEET))
                    },
                )

                AppScreen.SUBSCRIPTIONS -> SubscriptionsScreen(
                    onBack = { screen = AppScreen.HOME },
                    onAddSubscription = {
                        appViewModel.openSheet(PathState(EDITOR_SHEET, mapOf("tab" to "subscription")))
                    },
                )

                AppScreen.STATS -> StatsScreen(onBack = { screen = AppScreen.HOME })

                AppScreen.SETTINGS -> Column(
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = { screen = AppScreen.HOME }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = null,
                            )
                        }
                    }
                    Settings(
                        onTriedWidget = {},
                        onOpenCurrency = {
                            appViewModel.openSheet(PathState(CURRENCY_EDITOR))
                        },
                        onOpenCategories = {
                            appViewModel.openSheet(PathState(CATEGORIES_SHEET))
                        },
                        onOpenSubscriptions = { screen = AppScreen.SUBSCRIPTIONS },
                        activityResultRegistryOwner = activityResultRegistryOwner,
                    )
                }
            }

            // Sheets are declared last so they draw above the screens.
            BottomSheets(activityResultRegistryOwner)

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
            ) {
                SwipeableSnackbarHost(hostState = remember { appViewModel._snackbarHostState })
            }
        }
    }
}
