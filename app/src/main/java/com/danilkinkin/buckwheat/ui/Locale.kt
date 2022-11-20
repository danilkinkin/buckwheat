import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.danilkinkin.buckwheat.appLocale
import com.danilkinkin.buckwheat.appTheme
import com.danilkinkin.buckwheat.dataStore
import com.danilkinkin.buckwheat.ui.*
import kotlinx.coroutines.flow.first
import java.util.Locale

@Composable
fun OverrideLocalize(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val overrideLocale = LocalContext.current.appLocale

    DisposableEffect(overrideLocale) {
        Log.d("OverrideLocalize", "Change locale to ${overrideLocale?.language}")
        if (overrideLocale === null) return@DisposableEffect onDispose {  }

        context.resources.apply {
            val config = Configuration(configuration)

            context.createConfigurationContext(configuration)
            Locale.setDefault(overrideLocale)
            config.setLocale(overrideLocale)
            context.resources.updateConfiguration(config, displayMetrics)
        }

        onDispose {  }
    }

    content()
}

suspend fun switchLocale(context: Context, configuration: Configuration, localeCode: String) {
    context.dataStore.edit {
        it[stringPreferencesKey("locale")] = localeCode
    }

    context.appLocale = Locale(localeCode)
}

suspend fun syncLocale(context: Context) {
    val currentValue = context.dataStore.data.first()

    val localeCode = currentValue[stringPreferencesKey("locale")]

    val locale = if (localeCode !== null) {
        Locale(localeCode)
    } else {
        null
    }

    context.appLocale = locale
}