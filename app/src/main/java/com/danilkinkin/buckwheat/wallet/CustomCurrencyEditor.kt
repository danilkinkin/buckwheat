package com.danilkinkin.buckwheat.wallet

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
        Column() {
            Text(
                text = stringResource(R.string.currency_custom_title),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(24.dp)
            )
            Divider()
            Box(Modifier) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    value = selectCurrency.value.toString(),
                    onValueChange = { selectCurrency.value = it },
                    shape = TextFieldDefaults.filledShape,
                    colors = TextFieldDefaults.textFieldColors()
                )
            }
            Divider()
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

@Preview
@Composable
fun PreviewCustomCurrencyEditor() {
    BuckwheatTheme {
        CustomCurrencyEditorContent(
            defaultCurrency = "",
            onChange = { },
            onClose = { }
        )
    }
}