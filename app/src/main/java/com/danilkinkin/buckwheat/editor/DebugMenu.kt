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

@Composable
fun DebugMenu(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    appViewModel: AppViewModel = hiltViewModel(),
    onDailySummary: () -> Unit = {},
    onPeriodSummary: () -> Unit = {},
    onBoarding: () -> Unit = {},
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
                    onDailySummary()
                    onClose()
                },
            )
            ButtonRow(
                text = "Open period summary screen",
                onClick = {
                    onPeriodSummary()
                    onClose()
                },
            )
            ButtonRow(
                text = "Open onboarding screen",
                onClick = {
                    onBoarding()
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