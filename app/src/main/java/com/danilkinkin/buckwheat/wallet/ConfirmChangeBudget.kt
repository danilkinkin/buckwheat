package com.danilkinkin.buckwheat.wallet

import OverrideLocalize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.RenderAdaptivePane
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

@Composable
fun ConfirmChangeBudget(
    onConfirm: () -> Unit,
    onClose: () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .widthIn(max = 440.dp)
            .padding(36.dp)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_priority_high),
                    tint = LocalContentColor.current.copy(alpha = 0.7f),
                    contentDescription = null,
                )
            }
            Text(
                text = stringResource(R.string.confirm_change_budget_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.confirm_change_budget_description),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
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
                        onConfirm()
                        onClose()
                    },
                    colors = ButtonDefaults.textButtonColors(),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                ) {
                    Text(text = stringResource(R.string.confirm_change_budget))
                }
            }
        }
    }
}

@Composable
fun ConfirmChangeBudgetDialog(
    onConfirm: () -> Unit,
    onClose: () -> Unit,
) {
    Dialog(
        onDismissRequest = { onClose() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        OverrideLocalize {
            RenderAdaptivePane {
                ConfirmChangeBudget(
                    onConfirm = onConfirm,
                    onClose = onClose
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        ConfirmChangeBudget({}, {})
    }
}