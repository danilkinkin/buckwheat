package com.danilkinkin.buckwheat.base

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.danilkinkin.buckwheat.data.SystemBarState
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.ui.isNightMode
import com.danilkinkin.buckwheat.util.setSystemStyle
import com.danilkinkin.buckwheat.wallet.Wallet
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetWrapper(
    cancelable: Boolean = true,
    state: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    content: @Composable (() -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val localDensity = LocalDensity.current

    val statusBarHeight = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()

    setSystemStyle(
        style = {
            SystemBarState(
                statusBarColor = Color.Transparent,
                statusBarDarkIcons = false,
                navigationBarDarkIcons = true,
                navigationBarColor = Color.Transparent,
            )
        },
        key = state.targetValue,
        confirmChange = { state.targetValue !== ModalBottomSheetValue.Hidden },
    )

    val statusBarFillProgress = if (statusBarHeight == 0.dp) {
        0F
    } else {
        with(localDensity) { max(
            statusBarHeight - state.offset.value.roundToInt().toDp(),
            0.toDp(),
        ) } / statusBarHeight
    }

    ModalBottomSheetLayout(
        cancelable = cancelable,
        sheetBackgroundColor = MaterialTheme.colorScheme.surface,
        sheetState = state,
        sheetShape = MaterialTheme.shapes.extraLarge.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp),
            topStart = CornerSize(28.dp * (1F - statusBarFillProgress)),
            topEnd = CornerSize(28.dp * (1F - statusBarFillProgress)),
        ),
        sheetContent = {
            val isNightModeNow = isNightMode()

            setSystemStyle(
                style = {
                    SystemBarState(
                        statusBarColor = Color.Transparent,
                        statusBarDarkIcons = !isNightModeNow,
                        navigationBarDarkIcons = true,
                        navigationBarColor = Color.Transparent,
                    )
                },
                key = statusBarFillProgress,
                confirmChange = { statusBarFillProgress > 0.5F },
            )

            Box(
                modifier = Modifier
                    .padding(
                        top = statusBarHeight * statusBarFillProgress
                    )
            ) {
                content {
                    coroutineScope.launch {
                        state.hide()
                    }
                }
            }

            if (cancelable) {
                Box(
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Box(
                        Modifier
                            .height(4.dp)
                            .width(30.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                            .align(Alignment.Center)
                    )
                }
            }
        }
    ) {}

    BackHandler(state.isVisible) {
        if (cancelable) {
            coroutineScope.launch {
                state.hide()
            }
        }
    }

    LaunchedEffect(state.currentValue) {

    }
        coroutineScope.launch {
            state.hide()
        }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun PreviewBottomSheetWrapper() {
    val state = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()
    
    BuckwheatTheme {
        androidx.compose.material3.Surface(modifier = Modifier.fillMaxSize()) {
            androidx.compose.material3.Button(onClick = {
                coroutineScope.launch {
                    state.show()
                }
            }) {
                androidx.compose.material3.Text(text = "Show")
            }

            BottomSheetWrapper(state = state) {
                Wallet()
            }
        }
    }
}