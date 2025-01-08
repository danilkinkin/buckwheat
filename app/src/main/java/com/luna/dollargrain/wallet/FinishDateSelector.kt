package com.luna.dollargrain.wallet

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.R
import com.luna.dollargrain.base.LocalBottomSheetScrollState
import com.luna.dollargrain.base.datePicker.DatePicker
import com.luna.dollargrain.base.datePicker.model.CalendarSelectionMode
import com.luna.dollargrain.base.datePicker.model.CalendarState
import com.luna.dollargrain.base.datePicker.model.selectedDatesFormatted
import com.luna.dollargrain.ui.DollargrainTheme
import com.luna.dollargrain.util.countDays
import com.luna.dollargrain.util.toDate
import com.luna.dollargrain.util.toLocalDate
import java.time.LocalDate
import java.util.Date

const val FINISH_DATE_SELECTOR_SHEET = "finishDateSelector"

@Composable
fun FinishDateSelector(
    selectDate: Date? = null,
    onBackPressed: () -> Unit,
    onApply: (finishDate: Date) -> Unit,
) {
    val context = LocalContext.current
    val localBottomSheetScrollState = LocalBottomSheetScrollState.current

    Surface(modifier = Modifier.fillMaxSize().padding(top = localBottomSheetScrollState.topPadding)) {
        val calendarState = remember {
            CalendarState(
                context,
                selectionMode = CalendarSelectionMode.RANGE,
                selectDate = selectDate,
                disableBeforeDate = Date(),
            )
        }

        LaunchedEffect(Unit) {
            if (selectDate !== null) calendarState.setSelectedDay(selectDate.toLocalDate())
        }

        FinishDateSelectorContent(
            calendarState = calendarState,
            onDayClicked = { calendarState.setSelectedDay(it) },
            onBackPressed = onBackPressed,
            onApply = { onApply(calendarState.calendarUiState.value.selectedEndDate!!.toDate()) }
        )
    }
}

@Composable
private fun FinishDateSelectorContent(
    calendarState: CalendarState,
    onDayClicked: (LocalDate) -> Unit,
    onBackPressed: () -> Unit,
    onApply: () -> Unit,
) {
    Column {
        FinishDateSelectorTopAppBar(calendarState, onBackPressed, onApply)
        DatePicker(
            calendarState = calendarState,
            onDayClicked = onDayClicked,
        )
    }
}

@Composable
private fun FinishDateSelectorTopAppBar(
    calendarState: CalendarState,
    onBackPressed: () -> Unit,
    onApply: () -> Unit,
) {
    Surface {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = { onBackPressed() }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = null,
                    )
                }
                Spacer(Modifier.weight(1F))
                Button(
                    modifier = Modifier.padding(end = 4.dp),
                    onClick = { onApply() },
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    enabled = calendarState.calendarUiState.value.hasSelectedDates,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_apply),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)

                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = "confirm!")
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
                Column {
                    Text(
                        text = if (!calendarState.calendarUiState.value.hasSelectedDates) {
                            "choose an ending date"
                        } else {
                            selectedDatesFormatted(calendarState)
                        },
                        style = MaterialTheme.typography.titleLarge,
                    )
                    val days = if (calendarState.calendarUiState.value.hasSelectedDates) {
                        countDays(
                            calendarState.calendarUiState.value.selectedEndDate!!.toDate(),
                            calendarState.calendarUiState.value.selectedStartDate!!.toDate(),
                        )
                    } else {
                        0
                    }

                    Text(
                        text = if (days > 1) "$days days" else "$days day",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = if (calendarState.calendarUiState.value.hasSelectedDates) 0.6f else 0f),
                        ),
                    )
                }
            }
        }
    }
}


@Preview(name = "Default")
@Preview(name = "Default (Night mode)", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDefault(){
    DollargrainTheme {
        FinishDateSelector(onBackPressed = {}, onApply = {})
    }
}