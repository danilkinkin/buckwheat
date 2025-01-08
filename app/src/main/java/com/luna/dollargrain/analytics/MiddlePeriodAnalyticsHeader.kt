package com.luna.dollargrain.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.R
import com.luna.dollargrain.base.LocalBottomSheetScrollState
import com.luna.dollargrain.ui.DollargrainTheme

@Composable
fun MiddlePeriodAnalyticsHeader(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
) {
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current


    Box(Modifier.padding(top = localBottomSheetScrollState.topPadding)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = { onClose() },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.weight(1F))
            Text(
                text = "analytics",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.weight(1F))
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Preview
@Composable
private fun Preview() {
    DollargrainTheme {
        MiddlePeriodAnalyticsHeader()
    }
}