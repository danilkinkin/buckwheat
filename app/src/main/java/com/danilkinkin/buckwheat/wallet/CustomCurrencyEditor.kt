package com.danilkinkin.buckwheat.wallet

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.combineColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCurrencyEditorContent(
    defaultCurrency: String? = "",
    onChange: (currency: String) -> Unit,
    onClose: () -> Unit,
) {
    val selectCurrency = remember { mutableStateOf(defaultCurrency ?: "") }

    Card(
        shape = CardDefaults.shape,
        modifier = Modifier
            .widthIn(max = 500.dp)
            .padding(36.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.currency_custom_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(24.dp)
            )
            Box(Modifier) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    value = selectCurrency.value,
                    onValueChange = { selectCurrency.value = it },
                    shape = TextFieldDefaults.filledShape,
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = combineColors(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface,
                            0.5F
                        )
                    )
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
            ) {
                Button(
                    onClick = { onClose() },
                    colors = ButtonDefaults.textButtonColors(),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        onChange(selectCurrency.value!!)
                        onClose()
                    },
                    colors = ButtonDefaults.textButtonColors(),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    enabled = selectCurrency.value.trim() !== "",
                ) {
                    Text(text = stringResource(R.string.accept))
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomCurrencyEditor(
    defaultCurrency: String? = null,
    onChange: (currency: String) -> Unit,
    onClose: () -> Unit,
) {
    Dialog(
        onDismissRequest = { onClose() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CustomCurrencyEditorContent(
            defaultCurrency = defaultCurrency,
            onChange = onChange,
            onClose = { onClose() }
        )
    }
}

@Preview(name = "Default")
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        CustomCurrencyEditorContent(
            defaultCurrency = "",
            onChange = { },
            onClose = { }
        )
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        CustomCurrencyEditorContent(
            defaultCurrency = "",
            onChange = { },
            onClose = { }
        )
    }
}