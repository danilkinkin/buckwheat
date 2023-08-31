package com.danilkinkin.buckwheat.wallet

import OverrideLocalize
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.RenderAdaptivePane
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import kotlinx.coroutines.delay
import java.lang.Integer.min

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomCurrencyEditorContent(
    defaultCurrency: String? = "",
    onChange: (currency: String) -> Unit,
    onClose: () -> Unit,
) {
    var selectCurrency by remember { mutableStateOf(defaultCurrency ?: "") }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current


    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .widthIn(max = 500.dp)
            .padding(36.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.currency_custom_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.currency_custom_description),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
            )
            BasicTextField(
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth(),
                value = selectCurrency,
                onValueChange = {
                    val string = it
                        .trim()
                        .replace("\r|\n","")

                    selectCurrency = string.substring(0, min(4, string.length))
                },
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (selectCurrency.isEmpty()) return@KeyboardActions

                        onChange(selectCurrency)
                        onClose()
                    }
                ),
                decorationBox = { input ->
                    Row(
                        Modifier.padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Text(
                            text = "150",
                            style = MaterialTheme.typography.displayMedium,
                        )
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier.width(160.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (selectCurrency.isEmpty()) {
                                Text(
                                    text = "$",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                )
                            }
                            input()
                        }
                    }
                }
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                delay(100)
                keyboard?.show()
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
                        onChange(selectCurrency)
                        onClose()
                    },
                    colors = ButtonDefaults.textButtonColors(),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    enabled = selectCurrency.trim() !== "",
                ) {
                    Text(text = stringResource(R.string.accept))
                }
            }
        }
    }
}

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
        OverrideLocalize {
            RenderAdaptivePane {
                CustomCurrencyEditorContent(
                    defaultCurrency = defaultCurrency,
                    onChange = onChange,
                    onClose = { onClose() }
                )
            }
        }
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