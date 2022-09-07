package com.danilkinkin.buckwheat.wallet

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
import com.danilkinkin.buckwheat.calendar.Calendar
import com.danilkinkin.buckwheat.calendar.model.CalendarState
import com.danilkinkin.buckwheat.calendar.model.selectedDatesFormatted
import com.danilkinkin.buckwheat.ui.BuckwheatTheme
import com.danilkinkin.buckwheat.util.toDate
import com.danilkinkin.buckwheat.util.toLocalDate
import java.time.LocalDate
import java.util.*


@Composable
fun FinishDateSelector(
    selectDate: Date? = null,
    onBackPressed: () -> Unit,
    onApply: (finishDate: Date) -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        val calendarState = remember { CalendarState(selectDate) }

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
        Calendar(
            calendarState = calendarState,
            onDayClicked = onDayClicked,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinishDateSelectorTopAppBar(
    calendarState: CalendarState,
    onBackPressed: () -> Unit,
    onApply: () -> Unit,
) {
    Surface {
        MediumTopAppBar(
            navigationIcon = {
                IconButton(
                    onClick = { onBackPressed() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = null,
                    )
                }
            },
            title = {
                Text(
                    text = if (!calendarState.calendarUiState.value.hasSelectedDates) {
                        stringResource(R.string.select_finish_date_title)
                    } else {
                        selectedDatesFormatted(calendarState)
                    },
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            actions = {
                Button(
                    onClick = { onApply() },
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    modifier = Modifier.padding(end = 8.dp),
                    enabled = calendarState.calendarUiState.value.selectedStartDate !== null,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Localized description",
                        modifier = Modifier.size(ButtonDefaults.IconSize)

                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(text = stringResource(R.string.apply))
                }
            }

        )
    }
}


@Preview(name = "Default")
@Composable
private fun PreviewDefault(){
    BuckwheatTheme {
        FinishDateSelector(onBackPressed = {}, onApply = {})
    }
}

@Preview(name = "Night mode", uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNightMode(){
    BuckwheatTheme {
        FinishDateSelector(onBackPressed = {}, onApply = {})
    }
}