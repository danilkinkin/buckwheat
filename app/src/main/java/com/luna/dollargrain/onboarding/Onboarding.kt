package com.luna.dollargrain.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.LocalWindowInsets
import com.luna.dollargrain.base.LocalBottomSheetScrollState
import com.luna.dollargrain.ui.DollargrainTheme

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
                text = "Hii!",
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Welcome to Dollargrain :3",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Let's start saving together!",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(48.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                NumberedRow(
                    number = 1,
                    title = "Establish a budget",
                    subtitle = "Enter how much money you have, or how much you want to spend, and input it into Dollargrain. Select a time frame, and Dollargrain will help you establish a daily budget. "
                )
                NumberedRow(
                    number = 2,
                    title = "Record your expenses",
                    subtitle = "Dollargrain will assist you in calculating your daily budget, and show you analytics about your expenses"
                )
                NumberedRow(
                    number = 3,
                    title = "Spend wisely",
                    subtitle = "Over time, you will find out more about your spending habits, and allow yourself to better manage your finances"
                )
            }
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = {
                    onSetBudget()
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Let's go!",
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDefault() {
    DollargrainTheme {
        Onboarding()
    }
}