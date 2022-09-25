package com.danilkinkin.buckwheat.base

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.zIndex
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import java.lang.Float.max
import java.lang.Float.min
import kotlin.math.abs
import kotlin.math.roundToInt

enum class DeleteState { IDLE, DELETE }

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun rememberSwipeForDismiss(
    onDelete: () -> Unit = {},
): SwipeableState<DeleteState> = rememberSwipeableState(
    DeleteState.IDLE,
    confirmStateChange = {
        if (it == DeleteState.DELETE) onDelete()

        true
    }
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeForDismiss(
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.surface,
    onDelete: () -> Unit = {},
    swipeableState: SwipeableState<DeleteState> = rememberSwipeForDismiss(
        onDelete = { onDelete() }
    ),
    content: @Composable () -> Unit,
) {
    val localDensity = LocalDensity.current

    val width = remember { mutableStateOf(0F) }
    val height = remember { mutableStateOf(0F) }

    val anchors = if (width.value != 0F) {
        mapOf(0f to DeleteState.IDLE, -width.value to DeleteState.DELETE)
    } else {
        mapOf(0f to DeleteState.IDLE)
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                orientation = Orientation.Horizontal,
                thresholds = { _, _ -> FractionalThreshold(0.4f) },
            )
            .onGloballyPositioned {
                width.value = it.size.width.toFloat()
                height.value = it.size.height.toFloat()
            }
            .zIndex(if (swipeableState.offset.value.roundToInt() < 0f) 1f else 0f)
        ,
    ) {
        val size = with(LocalDensity.current) {
            if (swipeableState.offset.value > 0f) 0f.toDp()
            else max(min(16.dp.toPx(), abs(swipeableState.offset.value)), 0f).toDp()
        }

        if (swipeableState.offset.value < 0f) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(with(localDensity) { height.value.toDp() })
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete_forever),
                    tint = MaterialTheme.colorScheme.onError,
                    contentDescription = null,
                    modifier = Modifier
                        .offset(with(LocalDensity.current) {
                            (swipeableState.offset.value / 2).toDp() + 12.dp
                        })
                        .alpha((
                            with(LocalDensity.current) {
                                ((-swipeableState.offset.value - 24.dp.toPx()) / (48.dp.toPx()))
                            }
                        ).coerceIn(0f, 1f))
                )
            }
        }

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(with(localDensity) { height.value.toDp() })
                .offset {
                    IntOffset(
                       x = min(swipeableState.offset.value, 0f).roundToInt(),
                       y = 0,
                    )
                }
                .padding(vertical = min(size / 4f, 4.dp))
                .shadow(
                    min(size, 4.dp),
                    RoundedCornerShape(size),
                ),
            color = contentColor,
        ) {}
        
        Box(
            modifier = modifier
                .fillMaxWidth()
                .offset {
                    IntOffset(
                        x = min(swipeableState.offset.value, 0f).roundToInt(),
                        y = 0,
                    )
                }
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
private fun PreviewDefault() {
    BuckwheatTheme {
        SwipeForDismiss {
            Text(text = "Swipe to dismiss", modifier = Modifier.padding(24.dp))
        }
    }
}