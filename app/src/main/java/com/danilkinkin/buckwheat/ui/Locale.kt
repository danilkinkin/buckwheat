import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.danilkinkin.buckwheat.appLocale
import com.danilkinkin.buckwheat.settingsDataStore
import com.danilkinkin.buckwheat.systemLocale
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

class ContextWithUpdatedResources(
    private val resources: Context,
    base: Context
) : ContextWrapper(base) {
    override fun getResources(): Resources {
        return resources.resources
    }
}

@Composable
fun OverrideLocalize(content: @Composable () -> Unit) {
    val systemLocale = LocalContext.current.systemLocale!!
    val overrideLocale = LocalContext.current.appLocale ?: systemLocale
    val localContext = LocalContext.current

    val (context, configuration) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Pair(LocalContext.current, LocalConfiguration.current)
    } else {
        val config = Configuration(LocalConfiguration.current)

        val locales = listOf(overrideLocale).toMutableList()

        for (index in 0 until config.locales.size()) {
            if (config.locales[index].language !== overrideLocale.language) {
                locales.add(config.locales[index])
            }
        }

        config.setLocales(LocaleList(*(locales.toTypedArray())))
        config.setLayoutDirection(overrideLocale)
        Locale.setDefault(overrideLocale)

        Pair(
            ContextWithUpdatedResources(LocalContext.current.createConfigurationContext(config), localContext),
            config,
        )
    }

    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        LocalContext provides context,
    ) {
        content()
    }
}

suspend fun switchOverrideLocale(context: Context, localeCode: String?) {
    context.settingsDataStore.edit {
        if (localeCode != null) {
            it[stringPreferencesKey("locale")] = localeCode
        } else {
            it.minusAssign(stringPreferencesKey("locale"))
        }
    }

    context.appLocale = if (localeCode != null) Locale(localeCode) else null
}

fun syncOverrideLocale(context: Context) {
    context.systemLocale = context.resources.configuration.locales[0]

    val currentValue = runBlocking { context.settingsDataStore.data.first() }

    val localeCode = currentValue[stringPreferencesKey("locale")]

    val locale = if (localeCode !== null) {
        Locale(localeCode)
    } else {
        null
    }

    context.appLocale = locale
}