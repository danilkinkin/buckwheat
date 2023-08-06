package com.danilkinkin.buckwheat.editor.dateTimeEdit

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.datePicker.DatePicker
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarSelectionMode
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarState
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import com.danilkinkin.buckwheat.util.toLocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

@Composable
fun DateTimeEditPill(
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
) {
    if (spendsViewModel.editedSpent === null) return

    var date by remember {
        mutableStateOf(spendsViewModel.currentDate)
    }

    var isPickTime by remember { mutableStateOf(false) }
    var isPickDate by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        Row(
            modifier = Modifier
                .offset(x = 8.dp)
                .clip(CircleShape)
                .clickable { isPickDate = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(prettyDate(date, forceShowDate = true, showTime = false))
        }
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { isPickTime = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(prettyDate(date, forceHideDate = true))
        }
    }

    if (isPickTime) {
        TimePickerDialog(
            initTime = date.toLocalDateTime().toLocalTime(),
            onSelect = { hour, minute, is24Hour ->
                val cal: Calendar = Calendar.getInstance()
                cal.time = date
                val year: Int = cal.get(Calendar.YEAR)
                val month: Int = cal.get(Calendar.MONTH)
                val day: Int = cal.get(Calendar.DAY_OF_MONTH)

                cal.set(year, month, day, hour, minute)

                date = cal.time

                spendsViewModel.currentDate = date
                isPickTime = false
            },
            onClose = {
                isPickTime = false
            }
        )
    }

    if (isPickDate) {
        DatePickerDialog(
            initDate = date.toLocalDate(),
            disableBeforeDate = spendsViewModel.startDate.value!!.toLocalDate(),
            disableAfterDate = LocalDate.now(),
            onSelect = { newDate ->
                val cal: Calendar = Calendar.getInstance()
                cal.time = newDate.toDate()
                val year: Int = cal.get(Calendar.YEAR)
                val month: Int = cal.get(Calendar.MONTH)
                val day: Int = cal.get(Calendar.DAY_OF_MONTH)

                val calOriginal: Calendar = Calendar.getInstance()
                calOriginal.time = date
                calOriginal.set(
                    year,
                    month,
                    day,
                    calOriginal.get(Calendar.HOUR_OF_DAY),
                    calOriginal.get(Calendar.MINUTE),
                )

                date = calOriginal.time

                spendsViewModel.currentDate = date
                isPickDate = false
            },
            onClose = {
                isPickDate = false
            },
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .widthIn(max = 500.dp)
                .padding(36.dp)
            //.imePadding(),
        ) {
            Column(
                Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.change_time),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                        .fillMaxWidth(),
                )
                AnimatedContent(
                    targetState = inputMode,
                    transitionSpec = {
                        if (targetState && !initialState) {
                            slideInVertically(tween(durationMillis = 200)) { height -> height } + fadeIn(
                                tween(durationMillis = 200)
                            ) with
                                    slideOutVertically(tween(durationMillis = 200)) { height -> -height } + fadeOut(
                                tween(durationMillis = 200)
                            )
                        } else {
                            slideInVertically(tween(durationMillis = 200)) { height -> -height } + fadeIn(
                                tween(durationMillis = 200)
                            ) with
                                    slideOutVertically(tween(durationMillis = 200)) { height -> height } + fadeOut(
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
                            imageVector = if (inputMode) Icons.Outlined.Schedule else Icons.Outlined.Keyboard,
                            contentDescription = "Edit",
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = { onClose() }) {
                        Text(text = stringResource(R.string.cancel))
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
                        Text(text = stringResource(R.string.apply))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    initDate: LocalDate = LocalDate.now(),
    disableBeforeDate: LocalDate? = null,
    disableAfterDate: LocalDate? = null,
    onSelect: (date: LocalDate) -> Unit,
    onClose: () -> Unit,
) {
    val datePickerState = remember {
        CalendarState(
            CalendarSelectionMode.SINGLE,
            selectDate = initDate.toDate(),
            disableBeforeDate = disableBeforeDate?.toDate(),
            disableAfterDate = disableAfterDate?.toDate(),
        )
    }

    Dialog(
        onDismissRequest = { onClose() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        )
    ) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier
                .widthIn(max = 500.dp)
                .padding(16.dp)
            //.imePadding(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.change_date),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                )
                Text(
                    text = prettyDate(
                        date = datePickerState.calendarUiState.value.selectedStartDate!!.toDate(),
                        pattern = "EEEE, dd MMM",
                        simplifyIfToday = true,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(bottom = 16.dp, start = 24.dp, end = 24.dp)
                        .fillMaxWidth(),
                )
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    DatePicker(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        calendarState = datePickerState,
                        onDayClicked = {
                            datePickerState.setSelectedDay(it)
                        }
                    )
                }
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Row(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)) {
                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = { onClose() }) {
                        Text(text = stringResource(R.string.cancel))
                    }

                    TextButton(
                        onClick = {
                            onSelect(
                                datePickerState.calendarUiState.value.selectedStartDate!!
                            )
                        }
                    ) {
                        Text(text = stringResource(R.string.apply))
                    }
                }
            }
        }
    }
}