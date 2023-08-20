package com.danilkinkin.buckwheat.base.datePicker

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danilkinkin.buckwheat.base.datePicker.model.CalendarUiState
import com.danilkinkin.buckwheat.base.datePicker.model.Week
import com.danilkinkin.buckwheat.util.getWeek
import com.danilkinkin.buckwheat.util.prettyWeekDay
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters


@Composable
internal fun DaysOfWeek(modifier: Modifier = Modifier) {
    val week = getWeek()

    Row(modifier = modifier) {
        for (day in week) {
            DayOfWeekHeading(
                day = prettyWeekDay(day),
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
    var currentDay = beginningWeek.with(TemporalAdjusters.previousOrSame(getWeek()[0]))

    Box(Modifier.fillMaxWidth()) {
        Row(modifier = modifier) {
            for (i in 0..6) {
                if (currentDay.month == week.yearMonth.month) {
                    Day(
                        modifier = Modifier.weight(1f),
                        calendarState = calendarUiState,
                        day = currentDay,
                        onDayClicked = onDayClicked,
                    )
                } else {
                    Box(modifier = Modifier
                        .size(CELL_SIZE)
                        .weight(1f))
                }
                currentDay = currentDay.plusDays(1)
            }
        }
    }
}

internal val CELL_SIZE = 48.dp
