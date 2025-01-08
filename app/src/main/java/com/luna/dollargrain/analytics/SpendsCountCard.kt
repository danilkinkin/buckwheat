package com.luna.dollargrain.analytics

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.PathState
import com.luna.dollargrain.ui.BuckwheatTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendsCountCard(
    modifier: Modifier = Modifier,
    count: Int,
    appViewModel: AppViewModel = hiltViewModel(),
) {
    Card(
        modifier = modifier.height(IntrinsicSize.Min),
        shape = CircleShape,
        onClick = {
            appViewModel.openSheet(PathState(VIEWER_HISTORY_SHEET))
        }
    ) {
        val textColor = LocalContentColor.current

        Row(
            modifier = Modifier
                .padding(24.dp, 0.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(24.dp))

            Column(
                Modifier
                    .padding(14.dp, 8.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,

                    overflow = TextOverflow.Ellipsis,
                    softWrap = true,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.count_spends),
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor.copy(alpha = 0.6f),
                    softWrap = false,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        SpendsCountCard(
            count = 348,
        )
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode() {
    BuckwheatTheme {
        SpendsCountCard(
            count = 348,
        )
    }
}