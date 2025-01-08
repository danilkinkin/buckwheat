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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.LocalWindowInsets
import com.luna.dollargrain.base.LocalBottomSheetScrollState
import com.luna.dollargrain.ui.DollargrainTheme

const val NEW_DAY_BUDGET_DESCRIPTION_SHEET = "newDayBudgetDescription"

@Composable
fun NewDayBudgetDescription(onClose: () -> Unit = {}) {
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current
    val navigationBarHeight = androidx.compose.ui.unit.max(
        LocalWindowInsets.current.calculateBottomPadding(),
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
                    text = "new daily budget",
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
                text = "you overspent today :( this will be your new budget for the following days",
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
    DollargrainTheme {
        NewDayBudgetDescription()
    }
}
