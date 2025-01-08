package com.luna.dollargrain.editor.toolbar.restBudgetPill

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.R
import com.luna.dollargrain.LocalWindowInsets
import com.luna.dollargrain.base.ButtonRow
import com.luna.dollargrain.base.LocalBottomSheetScrollState
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.ui.DollargrainTheme

const val BUDGET_IS_OVER_DESCRIPTION_SHEET = "budgetIsOverDescription"

@Composable
fun BudgetIsOverDescription(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current
    val navigationBarHeight = androidx.compose.ui.unit.max(
        LocalWindowInsets.current.calculateBottomPadding(),
        16.dp,
    )

    val hideOverspendingWarn by spendsViewModel.hideOverspendingWarn.observeAsState(false)

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        Column(modifier = Modifier.padding(bottom = navigationBarHeight)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.budget_end),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            Text(
                modifier = Modifier
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 16.dp,
                    )
                    .fillMaxWidth(),
                text = stringResource(R.string.budget_end_description),
                style = MaterialTheme.typography.bodyMedium
                    .copy(color = LocalContentColor.current.copy(alpha = 0.6f)),
            )

            Spacer(modifier = Modifier.height(32.dp))

            ButtonRow(
                icon = if (hideOverspendingWarn) {
                    painterResource(R.drawable.ic_do_not_disturb)
                } else {
                    painterResource(R.drawable.ic_do_disturb)
                },
                text = stringResource(R.string.hide_overspending_warn),
                description = stringResource(R.string.hide_overspending_warn_description),
                wrapMainText = true,
                denseDescriptionOffset = false,
                onClick = {
                    spendsViewModel.hideOverspendingWarn(!hideOverspendingWarn)
                },
                endContent = {
                    Switch(
                        checked = hideOverspendingWarn,
                        onCheckedChange = {
                            spendsViewModel.hideOverspendingWarn(!hideOverspendingWarn)
                        },
                    )

                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    DollargrainTheme {
        BudgetIsOverDescription()
    }
}