package com.danilkinkin.buckwheat.settings

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.BuildConfig
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.base.TextRow
import com.danilkinkin.buckwheat.data.ExtendCurrency
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.export.rememberExportCSV
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

const val SETTINGS_SHEET = "settings"

@Composable
fun Settings(
    onTriedWidget: () -> Unit = {},
    onOpenCurrency: () -> Unit = {},
    onOpenCategories: () -> Unit = {},
    onOpenSubscriptions: () -> Unit = {},
    activityResultRegistryOwner: ActivityResultRegistryOwner? = null,
    spendsViewModel: SpendsViewModel = hiltViewModel(),
) {
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current
    val exportCSVLaunch = rememberExportCSV(
        activityResultRegistryOwner = activityResultRegistryOwner
    )

    val currency by spendsViewModel.currency.observeAsState(ExtendCurrency.none())
    val currencyCaption = when (currency.type) {
        ExtendCurrency.Type.NONE -> stringResource(R.string.currency_none)
        else -> currency.value.orEmpty()
    }

    val navigationBarHeight = androidx.compose.ui.unit.max(
        LocalWindowInsets.current.calculateBottomPadding(),
        16.dp,
    )

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = navigationBarHeight)
            ) {
                ThemeSwitcher()
                LangSwitcher()
                ButtonRow(
                    icon = painterResource(R.drawable.ic_currency),
                    text = stringResource(R.string.in_currency_label),
                    endCaption = currencyCaption,
                    onClick = onOpenCurrency,
                )
                ButtonRow(
                    icon = painterResource(R.drawable.ic_label),
                    text = stringResource(R.string.categories_title),
                    onClick = onOpenCategories,
                )
                ButtonRow(
                    icon = painterResource(R.drawable.ic_balance_wallet),
                    text = stringResource(R.string.subscriptions_title),
                    onClick = onOpenSubscriptions,
                )
                TryWidget(onTried = {
                    onTriedWidget()
                })
                ButtonRow(
                    icon = painterResource(R.drawable.ic_file_download),
                    text = stringResource(R.string.export_to_csv),
                    onClick = { exportCSVLaunch() },
                )
                TextRow(
                    text = stringResource(R.string.version, BuildConfig.VERSION_NAME),
                )
                About(Modifier.padding(start = 16.dp, end = 16.dp))
            }
        }
    }
}

@Preview(name = "Default")
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        Settings()
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        Settings()
    }
}
