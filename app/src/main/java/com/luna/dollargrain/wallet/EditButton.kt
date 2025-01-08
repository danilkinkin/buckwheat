package com.luna.dollargrain.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.R
import com.luna.dollargrain.ui.DollargrainTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    var size by remember { mutableStateOf(0.dp) }
    val localDensity = LocalDensity.current

    Box(
        Modifier
            .widthIn(max = 120.dp)
            .fillMaxHeight()
            .onGloballyPositioned {
                size = with(localDensity) { it.size.height.toDp() }
            }
    ) {
        Card(
            onClick = onClick,
            modifier = modifier.size(size),
            shape = CircleShape,
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = null,
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    DollargrainTheme {
        Box(
            modifier = Modifier
                .height(200.dp)
                .width(900.dp)
        ) {
            EditButton()
        }
    }
}