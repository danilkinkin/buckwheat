package com.danilkinkin.buckwheat.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.CheckedRow
import com.danilkinkin.buckwheat.base.TextRow
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import kotlinx.coroutines.launch
import switchLocale
import java.util.Locale

@Composable
fun LangSwitcher() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val coroutineScope = rememberCoroutineScope()

    fun handleSwitchLang(localeCode: String) {
        coroutineScope.launch {
            switchLocale(
                context,
                configuration,
                localeCode
            )
        }
    }

    val currentLocale = configuration.locales[0]
    val locales = listOf(
        Locale("ru"),
        Locale("en"),
        Locale("uk"),
        Locale("be"),
        Locale("sv"),
    )

    Column {
        TextRow(
            icon = painterResource(R.drawable.ic_language),
            text = stringResource(R.string.locale_label),
        )
        locales.forEach { locale ->
            CheckedRow(
                checked = locale.language === currentLocale.language,
                onValueChange = { handleSwitchLang(locale.language) },
                text = locale.displayName,
            )
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