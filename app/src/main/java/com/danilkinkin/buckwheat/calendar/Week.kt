package com.danilkinkin.buckwheat.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import com.danilkinkin.buckwheat.calendar.model.CalendarUiState
import com.danilkinkin.buckwheat.calendar.model.Week
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Composable
internal fun DaysOfWeek(modifier: Modifier = Modifier) {
    Row(modifier = modifier.clearAndSetSemantics { }) {
        for (day in DayOfWeek.values()) {
            DayOfWeekHeading(
                day = day.name.take(1),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun Week(
    calendarUiState: CalendarUiState,
    week: Week,
    onDayClicked: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val beginningWeek = week.yearMonth.atDay(1).plusWeeks(week.number.toLong())
    var currentDay = beginningWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    Box(Modifier.fillMaxWidth()) {
        Row(modifier = modifier) {
            for (i in 0..6) {
                if (currentDay.month == week.yearMonth.month) {
                    Day(
                        modifier = Modifier.weight(1f),
                        calendarState = calendarUiState,
                        day = currentDay,
                        onDayClicked = onDayClicked,
                        month = week.yearMonth
                    )
                } else {
                    Box(modifier = Modifier.size(CELL_SIZE).weight(1f))
                }
                currentDay = currentDay.plusDays(1)
            }
        }
    }
}

internal val CELL_SIZE = 48.dp
