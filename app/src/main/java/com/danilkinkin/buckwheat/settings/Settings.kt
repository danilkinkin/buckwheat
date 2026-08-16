package com.danilkinkin.buckwheat.settings

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
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
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.base.TextRow
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.patterns.PATTERN_INSIGHTS_SHEET
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.wallet.rememberImportCSV

const val SETTINGS_SHEET = "settings"

@Composable
fun Settings(
    appViewModel: AppViewModel = hiltViewModel(),
    onTriedWidget: () -> Unit = {},
) {
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current

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
                RoundValuesSetting()
                AppLockSetting()
                TextRow(
                    icon = painterResource(R.drawable.ic_notifications),
                    text = stringResource(R.string.notifications_title),
                    endIcon = painterResource(R.drawable.ic_arrow_right),
                    modifier = Modifier.clickable {
                        appViewModel.openSheet(
                            com.danilkinkin.buckwheat.data.PathState(NOTIFICATIONS_SHEET)
                        )
                    },
                )
                TextRow(
                    icon = painterResource(R.drawable.ic_mic),
                    text = stringResource(R.string.voice_ai_title),
                    endIcon = painterResource(R.drawable.ic_arrow_right),
                    modifier = Modifier.clickable {
                        appViewModel.openSheet(
                            com.danilkinkin.buckwheat.data.PathState(VOICE_AI_SETTINGS_SHEET)
                        )
                    },
                )
                TextRow(
                    icon = painterResource(R.drawable.ic_analytics),
                    text = stringResource(R.string.ai_insight_title),
                    endIcon = painterResource(R.drawable.ic_arrow_right),
                    modifier = Modifier.clickable {
                        appViewModel.openSheet(
                            com.danilkinkin.buckwheat.data.PathState(AI_INSIGHT_SHEET)
                        )
                    },
                )
                TextRow(
                    icon = painterResource(R.drawable.ic_equalizer),
                    text = stringResource(R.string.patterns_title),
                    endIcon = painterResource(R.drawable.ic_arrow_right),
                    modifier = Modifier.clickable {
                        appViewModel.openSheet(
                            com.danilkinkin.buckwheat.data.PathState(PATTERN_INSIGHTS_SHEET)
                        )
                    },
                )
                VoiceWidgetDesignSetting(appViewModel = appViewModel)
                CategoryWidgetDesignSetting(appViewModel = appViewModel)
                TryWidget(onTried = {
                    onTriedWidget()
                })
                TextRow(
                    icon = painterResource(R.drawable.ic_label),
                    text = stringResource(R.string.tags_management_title),
                    endIcon = painterResource(R.drawable.ic_arrow_right),
                    modifier = Modifier.clickable {
                        appViewModel.openSheet(
                            com.danilkinkin.buckwheat.data.PathState(TAGS_MANAGEMENT_SHEET)
                        )
                    },
                )
                TextRow(
                    icon = painterResource(R.drawable.ic_label),
                    text = stringResource(R.string.categories_management_title),
                    endIcon = painterResource(R.drawable.ic_arrow_right),
                    modifier = Modifier.clickable {
                        appViewModel.openSheet(
                            com.danilkinkin.buckwheat.data.PathState(CATEGORIES_MANAGEMENT_SHEET)
                        )
                    },
                )
                TextRow(
                    icon = painterResource(R.drawable.ic_money),
                    text = stringResource(R.string.category_caps_title),
                    endIcon = painterResource(R.drawable.ic_arrow_right),
                    modifier = Modifier.clickable {
                        appViewModel.openSheet(
                            com.danilkinkin.buckwheat.data.PathState(CATEGORY_CAPS_SHEET)
                        )
                    },
                )
                TextRow(
                    icon = painterResource(R.drawable.ic_autorenew),
                    text = stringResource(R.string.recurring_payments_title),
                    endIcon = painterResource(R.drawable.ic_arrow_right),
                    modifier = Modifier.clickable {
                        appViewModel.openSheet(
                            com.danilkinkin.buckwheat.data.PathState(RECURRING_PAYMENTS_SHEET)
                        )
                    },
                )
                TextRow(
                    icon = painterResource(R.drawable.ic_balance_wallet),
                    text = stringResource(R.string.goals_title),
                    endIcon = painterResource(R.drawable.ic_arrow_right),
                    modifier = Modifier.clickable {
                        appViewModel.openSheet(
                            com.danilkinkin.buckwheat.data.PathState(GOALS_SHEET)
                        )
                    },
                )
                TextRow(
                    icon = painterResource(R.drawable.ic_analytics),
                    text = stringResource(R.string.past_periods_title),
                    endIcon = painterResource(R.drawable.ic_arrow_right),
                    modifier = Modifier.clickable {
                        appViewModel.openSheet(
                            com.danilkinkin.buckwheat.data.PathState(PAST_PERIODS_SHEET)
                        )
                    },
                )
                TextRow(
                    icon = painterResource(R.drawable.ic_search),
                    text = stringResource(R.string.search_history_title),
                    endIcon = painterResource(R.drawable.ic_arrow_right),
                    modifier = Modifier.clickable {
                        appViewModel.openSheet(
                            com.danilkinkin.buckwheat.data.PathState(SEARCH_HISTORY_SHEET)
                        )
                    },
                )
                val importCSV = rememberImportCSV()
                TextRow(
                    icon = painterResource(R.drawable.ic_file_download),
                    text = stringResource(R.string.import_csv),
                    modifier = Modifier.clickable { importCSV() },
                    endIcon = painterResource(R.drawable.ic_arrow_right),
                )
                BackupRestoreSetting()
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
