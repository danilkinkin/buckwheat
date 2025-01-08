package com.luna.dollargrain.base.balloon

import android.graphics.PointF
import androidx.compose.runtime.Composable
import androidx.lifecycle.MutableLiveData

data class BalloonData(
    val id: Int,
    val content: @Composable () -> Unit,
    val anchor: PointF,
    val onClose: () -> Unit = { },
)

class BalloonController {
    var balloons = MutableLiveData<Map<Int, BalloonData>>(mapOf())
        private set
    var showedBalloons = MutableLiveData<Set<Int>>(setOf())
        private set
    private var nextId = 0

    fun spawn(
        content: @Composable () -> Unit = {},
        anchor: PointF = PointF(0f, 0f),
        onClose: () -> Unit = { },
    ): Int {
        balloons.value = balloons.value?.plus(
            nextId to BalloonData(
                id = nextId,
                content = content,
                anchor = anchor,
                onClose = onClose,
            )
        )

        return nextId++
    }

    fun show(
        tooltipId: Int,
    ) {
        showedBalloons.value = showedBalloons.value?.plus(tooltipId)
    }

    fun hide(
        tooltipId: Int,
    ) {
        showedBalloons.value = showedBalloons.value?.minus(tooltipId)
        balloons.value!![tooltipId]?.let {
            it.onClose()
        }
    }

    fun destroy(
        tooltipId: Int,
    ) {
        balloons.value = balloons.value?.minus(tooltipId)
    }
}