package com.danilkinkin.buckwheat.history

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.EmptyState
import com.danilkinkin.buckwheat.ui.BuckwheatTheme

@Composable
fun NoSpends(modifier: Modifier = Modifier) {
    EmptyState(
        icon = painterResource(R.drawable.ic_receipt),
        title = stringResource(R.string.no_spends),
        description = stringResource(R.string.no_spends_description),
        modifier = modifier,
    )
}

@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        NoSpends()
    }
}
