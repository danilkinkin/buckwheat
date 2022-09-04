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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.data.SystemBarState
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.setSystemStyle
import com.danilkinkin.buckwheat.wallet.Wallet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetWrapper(
    cancelable: Boolean = true,
    state: ModalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden),
    content: @Composable (() -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    setSystemStyle(
        style = SystemBarState(
            statusBarColor = Color.Transparent,
            statusBarDarkIcons = false,
            navigationBarDarkIcons = true,
            navigationBarColor = Color.Transparent,
        ),
        key = state.targetValue,
        confirmChange = { state.targetValue !== ModalBottomSheetValue.Hidden },
    )

    ModalBottomSheetLayout(
        cancelable = cancelable,
        sheetBackgroundColor = MaterialTheme.colorScheme.surface,
        sheetState = state,
        sheetShape = MaterialTheme.shapes.extraLarge.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp)),
        sheetContent = {
            content {
                coroutineScope.launch {
                    state.hide()
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

    BackHandler {
        coroutineScope.launch {
            state.hide()
        }
    }

    LaunchedEffect(state.currentValue) {

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