package com.danilkinkin.buckwheat.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.ButtonRow
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

const val BUDGET_IS_OVER_DESCRIPTION_SHEET = "budgetIsOverDescription"

@Composable
fun BudgetIsOverDescription(
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    onClose: () -> Unit = {},
) {
    val navigationBarHeight = androidx.compose.ui.unit.max(
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        16.dp,
    )

    val overspendingWarnHidden by spendsViewModel.overspendingWarnHidden.observeAsState(false)

    Surface {
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
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                text = stringResource(R.string.budget_end_description),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(32.dp))

            ButtonRow(
                text = stringResource(R.string.hide_overspending_warn),
                description = stringResource(R.string.hide_overspending_warn_description),
                onClick = {
                    spendsViewModel.hideOverspendingWarn(!overspendingWarnHidden)
                },
                endContent = {
                    Switch(
                        checked = overspendingWarnHidden,
                        onCheckedChange = {
                            spendsViewModel.hideOverspendingWarn(!overspendingWarnHidden)
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
    BuckwheatTheme {
        BudgetIsOverDescription()
    }
}