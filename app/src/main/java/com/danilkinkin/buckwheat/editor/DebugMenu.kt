package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.base.Divider
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.finishPeriod.FINISH_PERIOD_SHEET
import com.danilkinkin.buckwheat.onboarding.ON_BOARDING_SHEET
import com.danilkinkin.buckwheat.recalcBudget.RECALCULATE_DAILY_BUDGET_SHEET

const val DEBUG_MENU_SHEET = "debugMenu"

@Composable
fun DebugMenu(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val navigationBarHeight = androidx.compose.ui.unit.max(
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        16.dp,
    )

    Surface {
        Column(modifier = Modifier.padding(bottom = navigationBarHeight)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Debug menu",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Divider()
            ButtonRow(
                text = "Open daily summary screen",
                onClick = {
                    appViewModel.openSheet(PathState(RECALCULATE_DAILY_BUDGET_SHEET))
                    onClose()
                },
            )
            ButtonRow(
                text = "Open period summary screen",
                onClick = {
                    appViewModel.openSheet(PathState(FINISH_PERIOD_SHEET))
                    onClose()
                },
            )
            ButtonRow(
                text = "Open onboarding screen",
                onClick = {
                    appViewModel.openSheet(PathState(ON_BOARDING_SHEET))
                    onClose()
                },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        DebugMenu()
    }
}