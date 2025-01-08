package com.luna.dollargrain.editor.dateTimeEdit

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.luna.dollargrain.R
import com.luna.dollargrain.base.RenderAdaptivePane
import com.luna.dollargrain.ui.DollargrainTheme
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initTime: LocalTime = LocalTime.now(),
    onSelect: (hour: Int, minute: Int, is24Hour: Boolean) -> Unit,
    onClose: () -> Unit,
) {
    var inputMode by remember {
        mutableStateOf(false)
    }
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val timePickerState = remember {
        TimePickerState(initTime.hour, initTime.minute, is24Hour)
    }

    Dialog(
        onDismissRequest = { onClose() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        )
    ) {
        RenderAdaptivePane {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                modifier = Modifier
                    .padding(36.dp)
                    .imePadding(),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "change the time",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                            .fillMaxWidth(),
                    )
                    AnimatedContent(
                        label = "SwitchFromClockToInput",
                        targetState = inputMode,
                        transitionSpec = {
                            if (targetState && !initialState) {
                                fadeIn(
                                    tween(durationMillis = 200)
                                ) togetherWith
                                        fadeOut(
                                            tween(durationMillis = 200)
                                        )
                            } else {
                                fadeIn(
                                    tween(durationMillis = 200)
                                ) togetherWith
                                        fadeOut(
                                            tween(durationMillis = 200)
                                        )
                            }.using(
                                SizeTransform(clip = false)
                            )
                        }
                    ) { targetMode ->
                        if (targetMode) {
                            TimeInput(
                                state = timePickerState
                            )
                        } else {
                            TimePicker(
                                state = timePickerState
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { inputMode = !inputMode }) {
                            Icon(
                                painter = painterResource(
                                    if (inputMode) R.drawable.ic_clock else R.drawable.ic_keyboard
                                ),
                                contentDescription = "Edit",
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(onClick = { onClose() }) {
                            Text(text = "cancel")
                        }

                        TextButton(
                            onClick = {
                                onSelect(
                                    timePickerState.hour,
                                    timePickerState.minute,
                                    timePickerState.is24hour,
                                )
                            }
                        ) {
                            Text(text = "apply")
                        }
                    }
                }
            }
        }
    }
}


@Preview(name = "Default", widthDp = 540)
@Composable
private fun PreviewDefault(){
    DollargrainTheme {
        TimePickerDialog(
            initTime = LocalTime.now(),
            onSelect = { _, _, _ -> },
            onClose = {},
        )
    }
}

@Preview(name = "Night mode", widthDp = 540, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode(){
    DollargrainTheme {
        TimePickerDialog(
            initTime = LocalTime.now(),
            onSelect = { _, _, _ -> },
            onClose = {},
        )
    }
}