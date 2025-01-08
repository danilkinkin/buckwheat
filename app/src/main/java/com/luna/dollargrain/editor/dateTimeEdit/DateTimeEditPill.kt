package com.luna.dollargrain.editor.dateTimeEdit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.luna.dollargrain.data.AppViewModel
import com.luna.dollargrain.data.SpendsViewModel
import com.luna.dollargrain.editor.EditorViewModel
import com.luna.dollargrain.util.prettyDate
import com.luna.dollargrain.util.toDate
import com.luna.dollargrain.util.toLocalDate
import com.luna.dollargrain.util.toLocalDateTime
import java.time.LocalDate
import java.util.Calendar

@Composable
fun DateTimeEditPill(
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
) {
    if (editorViewModel.editedTransaction === null) return

    var cachedDate by remember { mutableStateOf(editorViewModel.currentDate) }
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
            Text(prettyDate(cachedDate, forceShowDate = true, showTime = false))
        }
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { isPickTime = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(prettyDate(cachedDate, forceHideDate = true))
        }
    }

    if (isPickTime) {
        TimePickerDialog(
            initTime = cachedDate.toLocalDateTime().toLocalTime(),
            onSelect = { hour, minute, _ ->
                val calendar = Calendar.getInstance()
                calendar.time = cachedDate
                calendar.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    hour,
                    minute,
                )

                cachedDate = calendar.time
                editorViewModel.currentDate = cachedDate
                isPickTime = false
            },
            onClose = {
                isPickTime = false
            }
        )
    }

    if (isPickDate) {
        DatePickerDialog(
            initDate = cachedDate.toLocalDate(),
            disableBeforeDate = spendsViewModel.startPeriodDate.value!!.toLocalDate(),
            disableAfterDate = LocalDate.now(),
            onSelect = { newDate ->
                val calendarNew = Calendar.getInstance()
                calendarNew.time = newDate.toDate()

                val calendar: Calendar = Calendar.getInstance()
                calendar.time = cachedDate
                calendar.set(
                    calendarNew.get(Calendar.YEAR),
                    calendarNew.get(Calendar.MONTH),
                    calendarNew.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                )

                cachedDate = calendar.time
                editorViewModel.currentDate = cachedDate
                isPickDate = false
            },
            onClose = {
                isPickDate = false
            },
        )
    }

}