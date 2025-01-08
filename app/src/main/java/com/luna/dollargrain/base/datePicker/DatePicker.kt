package com.luna.dollargrain.base.datePicker

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.luna.dollargrain.base.datePicker.model.CalendarState
import com.luna.dollargrain.base.datePicker.model.CalendarUiState
import com.luna.dollargrain.base.datePicker.model.Month
import com.luna.dollargrain.ui.DollargrainTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.luna.dollargrain.util.getWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

// TODO: add chips for months/days etc

@Composable
fun DatePicker(
    calendarState: CalendarState,
    onDayClicked: (date: LocalDate) -> Unit,
    modifier: Modifier = Modifier,

) {
    val calendarUiState = calendarState.calendarUiState.value
    val dayWidth = remember { mutableStateOf(CELL_SIZE) }

    val localDensity = LocalDensity.current

    LazyColumn(
        modifier = modifier
            .onGloballyPositioned {
                dayWidth.value = with(localDensity) { it.size.width.toDp() / 7 }
            },
    ) {
        calendarState.listMonths.forEach { month ->
            itemsCalendarMonth(calendarUiState, onDayClicked, month, dayWidth.value)
        }

        item(key = "bottomSpacer") {
            Spacer(
                modifier = Modifier.windowInsetsBottomHeight(
                    WindowInsets.navigationBars
                )
            )
        }
    }
}

private fun LazyListScope.itemsCalendarMonth(
    calendarUiState: CalendarUiState,
    onDayClicked: (LocalDate) -> Unit,
    month: Month,
    dayWidth: Dp,
) {
    item(month.yearMonth.month.name + month.yearMonth.year + "header") {
        MonthHeader(
            modifier = Modifier.padding(top = 32.dp),
            yearMonth = month.yearMonth
        )
    }

    // Expanding width and centering horizontally
    val contentModifier = Modifier.fillMaxWidth()

    item(month.yearMonth.month.name + month.yearMonth.year + "daysOfWeek") {
        DaysOfWeek(modifier = contentModifier)
    }

    // A custom key needs to be given to these items so that they can be found in tests that
    // need scrolling. The format of the key is ${year/month/weekNumber}. Thus,
    // the key for the fourth week of December 2020 is "2020/12/4"
    itemsIndexed(month.weeks, key = { index, _ ->
        month.yearMonth.year.toString() + "/" + month.yearMonth.month.value + "/" + (index + 1).toString()
    }) { _, week ->
        val beginningWeek = week.yearMonth.atDay(1).plusWeeks(week.number.toLong())
        val currentDay = beginningWeek.with(TemporalAdjusters.previousOrSame(getWeek()[0]))

        if (
            calendarUiState.hasSelectedPeriodOverlap(currentDay, currentDay.plusDays(6))
        ) {
            WeekSelectionPill(
                state = calendarUiState,
                currentWeekStart = currentDay,
                widthPerDay = dayWidth,
                heightPerDay = CELL_SIZE,
                week = week,
            )
        }
        Week(
            calendarUiState = calendarUiState,
            modifier = contentModifier,
            week = week,
            onDayClicked = onDayClicked
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Preview(name = "EN locale", locale = "en")
@Composable
fun DayPreview() {
    val context = LocalContext.current

    val state = remember { mutableStateOf(CalendarState(context)) }

    DollargrainTheme {
        Surface {
            DatePicker(
                state.value,
                onDayClicked = { state.value.setSelectedDay(it) },
            )
        }
    }
}
