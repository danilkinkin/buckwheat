package com.danilkinkin.buckwheat.editor.dateTimeEdit

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.danilkinkin.buckwheat.base.datePicker.DatePicker
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarState
import com.danilkinkin.buckwheat.data.AppViewModel
import com.danilkinkin.buckwheat.data.PathState
import com.danilkinkin.buckwheat.data.SpendsViewModel
import com.danilkinkin.buckwheat.util.prettyDate
import com.danilkinkin.buckwheat.util.toDate
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DateTimeEditPill(
    appViewModel: AppViewModel = hiltViewModel(),
    spendsViewModel: SpendsViewModel = hiltViewModel(),
) {
    if (spendsViewModel.editedSpent === null) return

    var date by remember {
        mutableStateOf(spendsViewModel.currentDate)
    }

    val mCalendar = Calendar.getInstance()
    val mHour = mCalendar[Calendar.HOUR_OF_DAY]
    val mMinute = mCalendar[Calendar.MINUTE]
    val mTimePickerDialog = TimePickerDialog(
        LocalContext.current,
        {_, mHour : Int, mMinute: Int ->

            val cal: Calendar = Calendar.getInstance()
            cal.time = date
            val year: Int = cal.get(Calendar.YEAR)
            val month: Int = cal.get(Calendar.MONTH)
            val day: Int = cal.get(Calendar.DAY_OF_MONTH)

            cal.set(year, month, day, mHour, mMinute)

            date = cal.time

            spendsViewModel.currentDate = date
        }, mHour, mMinute, false
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    appViewModel.openSheet(PathState(
                        name = DATE_SELECTOR_SHEET,
                        args = mapOf(
                            "initialDate" to date,
                            "disableBeforeDate" to spendsViewModel.startDate.value,
                            "disableAfterDate" to LocalDate
                                .now()
                                .toDate(),
                        ),
                        callback = { result ->
                            if (!result.containsKey("date")) return@PathState

                            val cal: Calendar = Calendar.getInstance()
                            cal.time = result["date"] as Date
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
                        }
                    ))
                }
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(prettyDate(date, forceShowDate = true, showTime = false))
        }
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    mTimePickerDialog.show()
                }
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(prettyDate(date, forceHideDate = true))
        }
    }


}

@Composable
private fun DateSelectorContent(
    calendarState: CalendarState,
    onDayClicked: (LocalDate) -> Unit,
    onBackPressed: () -> Unit,
    onApply: () -> Unit,
) {
    Column {
        DatePicker(
            calendarState = calendarState,
            onDayClicked = onDayClicked,
        )
    }
}