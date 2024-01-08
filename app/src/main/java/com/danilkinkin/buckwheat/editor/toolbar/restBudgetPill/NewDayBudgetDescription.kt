package com.danilkinkin.buckwheat.editor.toolbar.restBudgetPill

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

const val NEW_DAY_BUDGET_DESCRIPTION_SHEET = "newDayBudgetDescription"

@Composable
fun NewDayBudgetDescription(onClose: () -> Unit = {}) {
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current
    val navigationBarHeight = androidx.compose.ui.unit.max(
        WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        16.dp,
    )

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        Column(modifier = Modifier.padding(bottom = navigationBarHeight)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.new_daily_budget),
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
                text = stringResource(R.string.new_daily_budget_description),
                style = MaterialTheme.typography.bodyMedium
                    .copy(color = LocalContentColor.current.copy(alpha = 0.6f)),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        NewDayBudgetDescription()
    }
}
