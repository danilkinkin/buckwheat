package com.luna.dollargrain.wallet

import OverrideLocalize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.danilkinkin.dollargrain.R
import com.luna.dollargrain.base.RenderAdaptivePane
import com.luna.dollargrain.ui.BuckwheatTheme


@Composable
fun ConfirmFinishEarly(
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
                text = stringResource(R.string.confirm_finish_budget_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.confirm_finish_budget_description),
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
                    Text(text = stringResource(R.string.confirm_finish_budget))
                }
            }
        }
    }
}

@Composable
fun ConfirmFinishEarlyDialog(
    onConfirm: () -> Unit,
    onClose: () -> Unit,
) {
    Dialog(
        onDismissRequest = { onClose() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        OverrideLocalize {
            RenderAdaptivePane {
                ConfirmFinishEarly(
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
        ConfirmFinishEarly({}, {})
    }
}
