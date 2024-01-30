package com.danilkinkin.buckwheat.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.LocalWindowInsets
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.DescriptionButton
import com.danilkinkin.buckwheat.base.LocalBottomSheetScrollState
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

const val ON_BOARDING_SHEET = "onBoarding"

@Composable
fun Onboarding(
    onSetBudget: () -> Unit = {},
    onClose: () -> Unit = {},
) {
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current
    val navigationBarHeight = LocalWindowInsets.current.calculateBottomPadding()
        .coerceAtLeast(16.dp)

    Surface(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = navigationBarHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.hello),
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(48.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                NumberedRow(
                    number = 1,
                    title = stringResource(R.string.help_set_budget_title),
                    subtitle = stringResource(R.string.help_set_budget_description),
                )
                NumberedRow(
                    number = 2,
                    title = stringResource(R.string.help_record_spends_title),
                    subtitle = stringResource(R.string.help_record_spends_description),
                )
                NumberedRow(
                    number = 3,
                    title = stringResource(R.string.help_good_luck_title),
                    subtitle = stringResource(R.string.help_good_luck_description),
                )
            }
            Spacer(Modifier.height(48.dp))
            DescriptionButton(
                title = { Text(stringResource(R.string.set_period_title)) },
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 32.dp),
                onClick = {
                    onSetBudget()
                    onClose()
                },
            )
        }
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        Onboarding()
    }
}