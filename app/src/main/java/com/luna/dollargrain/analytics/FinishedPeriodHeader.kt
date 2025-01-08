package com.luna.dollargrain.analytics

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.luna.dollargrain.R
import com.luna.dollargrain.base.LocalBottomSheetScrollState
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.util.combineColors

@Composable
fun FinishedPeriodHeader(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    hasSpends: Boolean = false,
) {
    val localDensity = LocalDensity.current
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current

    var headerSize by remember { mutableStateOf(Size(0.dp, 0.dp)) }
    val scroll = with(localDensity) { scrollState.value.toDp() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = localBottomSheetScrollState.topPadding.coerceAtLeast(36.dp))
            .onGloballyPositioned {
                headerSize = Size(
                    width = with(localDensity) { it.size.width.toDp() },
                    height = with(localDensity) { it.size.height.toDp() }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        val halfWidth = headerSize.width / 2
        val halfHeight = headerSize.height / 2

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
                .absoluteOffset(y = scroll * 0.25f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(36.dp))
            Text(
                text = "period ended",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            if (!hasSpends) {
                Text(
                    text = "woah you spent nothing.. are you using the app?",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(
                    text = "here are the stats for dis period!!",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(64.dp))
        }

        val starColor = combineColors(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.surface,
            0.5f,
        )

        val angleStar1 by rememberInfiniteTransition("angleStar1").animateFloat(
            label = "angleStar1",
            initialValue = -20f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(tween(10000), RepeatMode.Reverse)
        )

        val angleStar2 by rememberInfiniteTransition("angleStar2").animateFloat(
            label = "angleStar2",
            initialValue = -50f,
            targetValue = 50f,
            animationSpec = infiniteRepeatable(tween(18000), RepeatMode.Reverse)
        )

        Icon(
            modifier = Modifier
                .requiredSize(256.dp)
                .absoluteOffset(x = halfWidth * 0.7f, y = -halfHeight * 0.6f + scroll * 0.35f)
                .rotate(angleStar1)
                .zIndex(-1f),
            painter = painterResource(R.drawable.shape_soft_star_1),
            tint = starColor,
            contentDescription = null,
        )
        Icon(
            modifier = Modifier
                .requiredSize(256.dp)
                .absoluteOffset(x = -halfWidth * 0.7f, y = halfHeight * 0.6f + scroll * 0.6f)
                .rotate(angleStar2)
                .zIndex(-1f),
            painter = painterResource(R.drawable.shape_soft_star_2),
            tint = starColor,
            contentDescription = null,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    DollargrainTheme {
        FinishedPeriodHeader()
    }
}