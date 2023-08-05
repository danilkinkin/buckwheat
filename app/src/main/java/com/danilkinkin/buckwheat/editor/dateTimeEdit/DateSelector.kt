package com.danilkinkin.buckwheat.editor.dateTimeEdit

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.base.datePicker.DatePicker
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarSelectionMode
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarState
import com.danilkinkin.buckwheat.base.datePicker.model.selectedDatesFormatted
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import java.time.LocalDate
import java.util.*

const val DATE_SELECTOR_SHEET = "dateSelector"

@Composable
fun DateSelector(
    selectDate: Date? = null,
    disableBeforeDate: Date? = null,
    disableAfterDate: Date? = null,
    onBackPressed: () -> Unit,
    onApply: (finishDate: Date) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        val calendarState = remember {
            CalendarState(
                selectionMode = CalendarSelectionMode.SINGLE,
                selectDate,
                disableBeforeDate,
                disableAfterDate,
            )
        }

        LaunchedEffect(Unit) {
            if (selectDate !== null) calendarState.setSelectedDay(selectDate.toLocalDate())
        }

        DateSelectorContent(
            calendarState = calendarState,
            onDayClicked = { calendarState.setSelectedDay(it) },
            onBackPressed = onBackPressed,
            onApply = { onApply(calendarState.calendarUiState.value.selectedStartDate!!.toDate()) }
        )
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
        DateSelectorTopAppBar(calendarState, onBackPressed, onApply)
        DatePicker(
            calendarState = calendarState,
            onDayClicked = onDayClicked,
        )
    }
}

@Composable
private fun DateSelectorTopAppBar(
    calendarState: CalendarState,
    onBackPressed: () -> Unit,
    onApply: () -> Unit,
) {
    Surface {
        Column() {
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                IconButton(
                    onClick = { onBackPressed() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null,
                    )
                }
                Spacer(Modifier.weight(1F))
                Button(
                    onClick = { onApply() },
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    enabled = calendarState.calendarUiState.value.hasSelectedDates,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)

                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = stringResource(R.string.apply))
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 24.dp,
                    )
            ) {
                Text(
                    text = if (!calendarState.calendarUiState.value.hasSelectedDates) {
                        stringResource(R.string.select_finish_date_title)
                    } else {
                        selectedDatesFormatted(calendarState)
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}


@Preview(name = "Default")
@Composable
private fun PreviewDefault(){
    BuckwheatTheme {
        DateSelector(onBackPressed = {}, onApply = {})
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode(){
    BuckwheatTheme {
        DateSelector(onBackPressed = {}, onApply = {})
    }
}