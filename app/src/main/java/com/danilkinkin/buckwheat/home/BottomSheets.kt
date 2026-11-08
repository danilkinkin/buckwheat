package com.danilkinkin.buckwheat.home

import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.base.BottomSheetWrapper
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.editor.EDITOR_SHEET
import com.danilkinkin.buckwheat.editor.EditorSheet
import com.danilkinkin.buckwheat.settings.BUG_REPORTER_SHEET
import com.danilkinkin.buckwheat.settings.BugReporter
import com.danilkinkin.buckwheat.settings.CATEGORIES_SHEET
import com.danilkinkin.buckwheat.settings.CategoriesSheet
import com.danilkinkin.buckwheat.settings.SETTINGS_CHANGE_LOCALE_SHEET
import com.danilkinkin.buckwheat.settings.SETTINGS_CHANGE_THEME_SHEET
import com.danilkinkin.buckwheat.settings.SETTINGS_TRY_WIDGET_SHEET
import com.danilkinkin.buckwheat.settings.LangSwitcherDialog
import com.danilkinkin.buckwheat.settings.ThemeSwitcherDialog
import com.danilkinkin.buckwheat.settings.TryWidgetDialog
import com.danilkinkin.buckwheat.wallet.CURRENCY_EDITOR
import com.danilkinkin.buckwheat.wallet.CurrencyEditor
import kotlinx.coroutines.launch

/**
 * The app's sheets, declared in one place so stacking order is explicit: later sheets draw above
 * earlier ones. Only sheets reachable from the current UI are kept here.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheets(
    activityResultRegistryOwner: ActivityResultRegistryOwner?,
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()

    BottomSheetWrapper(
        name = EDITOR_SHEET,
    ) { state ->
        EditorSheet(
            initialTab = (state.args["tab"] as? String) ?: "expense",
            onClose = {
                coroutineScope.launch {
                    state.hide()
                }
            }
        )
    }

    // Declared after the editor so these sheets draw above it.
    BottomSheetWrapper(
        name = CATEGORIES_SHEET,
    ) { state ->
        CategoriesSheet(
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
        name = BUG_REPORTER_SHEET,
    ) { state ->
        BugReporter(
            onClose = {
                coroutineScope.launch {
                    state.hide()
                }
            },
        )
    }

    BottomSheetWrapper(
        name = SETTINGS_CHANGE_THEME_SHEET,
    ) {
        ThemeSwitcherDialog()
    }

    BottomSheetWrapper(
        name = SETTINGS_CHANGE_LOCALE_SHEET,
    ) { state ->
        LangSwitcherDialog(
            onClose = {
                coroutineScope.launch {
                    state.hide()
                }
            }
        )
    }

    BottomSheetWrapper(
        name = SETTINGS_TRY_WIDGET_SHEET,
    ) {
        TryWidgetDialog()
    }
}
