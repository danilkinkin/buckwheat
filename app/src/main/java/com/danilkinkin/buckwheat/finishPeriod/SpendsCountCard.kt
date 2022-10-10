package com.danilkinkin.buckwheat.finishPeriod

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

@Composable
fun SpendsCountCard(
    modifier: Modifier = Modifier,
    count: Int,
) {
    Card(
        modifier = modifier.height(IntrinsicSize.Min),
        shape = CircleShape,
    ) {
        val textColor = LocalContentColor.current

        Column(
            Modifier.padding(vertical = 8.dp, horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.W900,
                overflow = TextOverflow.Ellipsis,
                softWrap = true,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.count_spends),
                style = MaterialTheme.typography.labelLarge,
                fontSize = MaterialTheme.typography.labelLarge.fontSize,
                color = textColor.copy(alpha = 0.6f),
                softWrap = false,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
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