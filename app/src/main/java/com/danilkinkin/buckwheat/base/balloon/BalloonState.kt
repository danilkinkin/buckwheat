package com.danilkinkin.buckwheat.base.balloon

import android.graphics.PointF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.data.AppViewModel

@Composable
fun rememberBalloonState(
    appViewModel: AppViewModel = hiltViewModel(),
): BalloonState {
    return remember {
        BalloonState(
            appViewModel = appViewModel,
        )
    }
}

class BalloonState(
    val appViewModel: AppViewModel
) {
    private var anchor: PointF = PointF(0f, 0f)
    private var content: @Composable () -> Unit = {}
    private var onClose: () -> Unit = {}
    private var tooltipId: Int = -1

    fun show() {
        tooltipId = appViewModel.balloonController.spawn(
            content = content,
            anchor = anchor,
            onClose = onClose,
        )
    }

    fun hide() {
        appViewModel.balloonController.hide(tooltipId)
    }

    fun setContent(
        newContent: @Composable () -> Unit,
    ) {
        content = newContent
    }

    fun setAnchor(
        x: Float,
        y: Float,
    ) {
        anchor = PointF(x, y)
    }

    fun setOnClose(
        newOnClose: () -> Unit,
    ) {
        onClose = newOnClose
    }
}