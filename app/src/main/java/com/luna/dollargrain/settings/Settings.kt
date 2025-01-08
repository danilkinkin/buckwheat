package com.luna.dollargrain.settings

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.BuildConfig
import com.luna.dollargrain.base.TextRow
import com.luna.dollargrain.ui.DollargrainTheme

const val SETTINGS_SHEET = "settings"

@Composable
fun SettingsContent(onTriedWidget: () -> Unit = {}) {
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(onClose: () ->Unit, onTriedWidget: () -> Unit = {}) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha=0.3f)
    ) {
        SettingsContent(onTriedWidget)
    }
}

@Preview(name = "Default")
@Composable
private fun PreviewDefault() {
    DollargrainTheme {
        SettingsContent()
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    DollargrainTheme {
        SettingsContent()
    }
}
