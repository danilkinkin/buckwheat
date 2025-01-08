package com.luna.dollargrain.settings

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.BuildConfig
import com.luna.dollargrain.R
import com.luna.dollargrain.LocalWindowInsets
import com.luna.dollargrain.base.LocalBottomSheetScrollState
import com.luna.dollargrain.base.ModalBottomSheetValue
import com.luna.dollargrain.base.TextRow
import com.luna.dollargrain.ui.DollargrainTheme

const val SETTINGS_SHEET = "settings"

@Composable
fun SettingsContent(onTriedWidget: () -> Unit = {}) {
    Surface(Modifier.padding()) {
        val localBottomSheetScrollState = LocalBottomSheetScrollState.current

        val navigationBarHeight = androidx.compose.ui.unit.max(
            LocalWindowInsets.current.calculateBottomPadding(),
            16.dp,
        )

        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "settings :3",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                ThemeSwitcher()
                TryWidget(onTried = {
                    onTriedWidget()
                })
                TextRow(
                    text = "Version: ${BuildConfig.VERSION_NAME}"
                )
                About(Modifier.padding(start = 16.dp, end = 16.dp))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun Settings(onTriedWidget: () -> Unit = {}) {
    ModalBottomSheet(
        onDismissRequest = { },
        sheetState = rememberModalBottomSheetState(),

    ) {
        SettingsContent()
    }
}

@Preview(name = "Default")
@Composable
private fun PreviewDefault() {
    DollargrainTheme {
        Settings()
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    DollargrainTheme {
        Settings()
    }
}
