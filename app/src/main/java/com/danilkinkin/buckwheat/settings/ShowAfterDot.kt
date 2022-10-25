package com.danilkinkin.buckwheat.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

@Composable
fun ShowAfterDot(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
) {
    val roundValues by spendsViewModel.roundValues.observeAsState(true)

    Column {
        ButtonRow(
            text = stringResource(R.string.show_after_dot),
            description = stringResource(R.string.show_after_dot_description),
            onClick = { spendsViewModel.changeRoundMode(!roundValues) },
            endContent = {
                Switch(
                    checked = !roundValues,
                    onCheckedChange = { spendsViewModel.changeRoundMode(!roundValues) },
                )
            }
        )
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        Surface {
            ShowAfterDot()
        }
    }
}