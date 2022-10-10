package com.danilkinkin.buckwheat.finishPeriod

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

@OptIn(ExperimentalUnitApi::class)
@Composable
fun AdviceCard(
    modifier: Modifier = Modifier,
) {
    val textColor = LocalContentColor.current

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            Modifier
                .padding(vertical = 24.dp, horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.advice_title),
                style = MaterialTheme.typography.displayMedium,
                fontSize = MaterialTheme.typography.displayMedium.fontSize,
                fontWeight = FontWeight.W900,
                overflow = TextOverflow.Ellipsis,
                softWrap = true,
                textAlign = TextAlign.Center,
                lineHeight = TextUnit(1f, TextUnitType.Em)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.advice_description),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                color = textColor.copy(alpha = 0.6f),
                softWrap = true,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Preview
@Composable
private fun Preview() {
    BuckwheatTheme {
        AdviceCard()
    }
}