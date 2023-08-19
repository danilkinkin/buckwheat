package com.danilkinkin.buckwheat.base.balloon

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow

@Composable
fun BalloonScope(
    modifier: Modifier = Modifier,
    balloonState: BalloonState,
    content: @Composable () -> Unit,
    onClose: () -> Unit = { },
    children: @Composable () -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier.onGloballyPositioned {
            balloonState.setContent(content)
            balloonState.setAnchor(
                it.positionInWindow().x + it.size.width / 2f,
                it.positionInWindow().y + it.size.height,
            )
            balloonState.setOnClose(onClose)
        },
    ) {
        children()
    }
}