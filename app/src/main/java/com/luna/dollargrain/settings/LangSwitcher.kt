package com.luna.dollargrain.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.dollargrain.R
import com.luna.dollargrain.LocalWindowInsets
import com.luna.dollargrain.appLocale
import com.luna.dollargrain.base.ButtonRow
import com.luna.dollargrain.base.CheckedRow
import com.luna.dollargrain.base.LocalBottomSheetScrollState
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.PathState
import com.luna.dollargrain.ui.BuckwheatTheme
import com.luna.dollargrain.util.titleCase
import kotlinx.coroutines.launch
import switchOverrideLocale
import java.util.Locale

const val SETTINGS_CHANGE_LOCALE_SHEET = "settings.changeLocale"

@Composable
fun LangSwitcher(appViewModel: AppViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val currentLocale = configuration.locales[0]

    ButtonRow(
        icon = painterResource(R.drawable.ic_language),
        text = stringResource(R.string.locale_label),
        onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                try {
                    val intent = Intent(android.provider.Settings.ACTION_APP_LOCALE_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    startActivity(context, intent, null)
                } catch (error: Error) {
                    appViewModel.openSheet(PathState(SETTINGS_CHANGE_LOCALE_SHEET))
                }
            } else {
                appViewModel.openSheet(PathState(SETTINGS_CHANGE_LOCALE_SHEET))
            }
        },
        endCaption = currentLocale.displayName.titleCase(),
    )
}

@Composable
fun LangSwitcherDialog(onClose: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val overrideLocale = LocalContext.current.appLocale
    val coroutineScope = rememberCoroutineScope()
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current

    val navigationBarHeight = androidx.compose.ui.unit.max(
        LocalWindowInsets.current.calculateBottomPadding(),
        16.dp,
    )

    fun handleSwitchLang(localeCode: String?) {
        coroutineScope.launch {
            switchOverrideLocale(context, localeCode)
            onClose()
        }
    }

    val currentLocale = configuration.locales[0]
    val locales = listOf(
        Locale("be"),
        Locale("cs"),
        Locale("de"),
        Locale("en"),
        Locale("es"),
        Locale("fr"),
        Locale("ia"),
        Locale("it"),
        Locale("ja"),
        Locale("pt", "BR"),
        Locale("ro"),
        Locale("ru"),
        Locale("sr"),
        Locale("sv"),
        Locale("ta"),
        Locale("tr"),
        Locale("uk"),
        Locale("zh", "CN"),
        Locale("pl"),
        Locale("sk", "SK"),
    )

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        Column(modifier = Modifier.padding(bottom = navigationBarHeight)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.locale_label),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            CheckedRow(
                text = stringResource(R.string.locale_system),
                checked = overrideLocale === null,
                onValueChange = { handleSwitchLang(null) },
            )
            locales.forEach { locale ->
                CheckedRow(
                    text = locale.getDisplayName(locale).titleCase(),
                    checked = overrideLocale !== null && locale.language === currentLocale.language,
                    onValueChange = { handleSwitchLang(locale.language) },
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        Surface {
            LangSwitcher()
        }
    }
}
