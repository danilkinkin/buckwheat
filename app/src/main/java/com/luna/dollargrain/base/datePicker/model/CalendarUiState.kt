package com.luna.dollargrain.base.datePicker.model

import androidx.compose.runtime.Composable
import com.luna.dollargrain.util.isSameDay
import com.luna.dollargrain.util.prettyDate
import com.luna.dollargrain.util.toDate
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs

@Composable
fun selectedDatesFormatted(state: CalendarState): String {
    val uiState = state.calendarUiState.value

    if (uiState.selectedStartDate == null) return ""

    var output =
        prettyDate(uiState.selectedStartDate.toDate(), showTime = false, forceShowDate = true)

    if (uiState.selectionMode == CalendarSelectionMode.SINGLE) return output

    output += if (uiState.selectedEndDate != null) {
        " — ${
            prettyDate(
                uiState.selectedEndDate.toDate(),
                showTime = false,
                forceShowDate = true
            )
        }"
    } else {
        " - ?"
    }

    return output
}

enum class CalendarSelectionMode {
    SINGLE,
    RANGE,
}

data class CalendarUiState(
    val selectionMode: CalendarSelectionMode = CalendarSelectionMode.RANGE,
    val selectedStartDate: LocalDate? = null,
    val selectedEndDate: LocalDate? = null,
    val disabledBefore: LocalDate? = LocalDate.now(),
    val disabledAfter: LocalDate? = null,
) {

    val hasSelectedDates: Boolean
        get() {
            return selectedEndDate != null || selectionMode == CalendarSelectionMode.SINGLE
        }

    fun hasSelectedPeriodOverlap(start: LocalDate, end: LocalDate): Boolean {
        if (!hasSelectedDates) return false
        if (selectedStartDate == null && selectedEndDate == null) return false
        if (selectedStartDate == start || selectedStartDate == end) return true
        if (selectedEndDate == null) {
            return !selectedStartDate!!.isBefore(start) && !selectedStartDate.isAfter(end)
        }
        return !end.isBefore(selectedStartDate) && !start.isAfter(selectedEndDate)
    }

    fun isDateInSelectedPeriod(date: LocalDate): Boolean {
        if (selectedStartDate == null) return false
        if (selectedStartDate == date) return true
        if (selectedEndDate == null) return false
        if (date.isBefore(selectedStartDate) ||
            date.isAfter(selectedEndDate)
        ) return false
        return true
    }

    fun isCurrentDay(date: LocalDate): Boolean {
        return isSameDay(
            date.toDate().time,
            Date().time,
        )
    }

    fun isDisabledDay(date: LocalDate): Boolean {
        return (disabledBefore != null && date.isBefore(disabledBefore))
                || (disabledAfter != null && date.isAfter(disabledAfter))
    }

    fun getNumberSelectedDaysInWeek(currentWeekStartDate: LocalDate, month: YearMonth): Int {
        var countSelected = 0
        var currentDate = currentWeekStartDate
        for (i in 0 until CalendarState.DAYS_IN_WEEK) {
            if (isDateInSelectedPeriod(currentDate) && currentDate.month == month.month) {
                countSelected++
            }
            currentDate = currentDate.plusDays(1)
        }
        return countSelected
    }

    /**
     * Returns the number of selected days from the start or end of the week, depending on direction.
     */
    fun selectedStartOffset(currentWeekStartDate: LocalDate, yearMonth: YearMonth): Int {
        var startDate = currentWeekStartDate
        var startOffset = 0
        for (i in 0 until CalendarState.DAYS_IN_WEEK) {
            if (!isDateInSelectedPeriod(startDate) || startDate.month != yearMonth.month) {
                startOffset++
            } else {
                break
            }
            startDate = startDate.plusDays(1)
        }

        return startOffset
    }

    fun isLeftHighlighted(beginningWeek: LocalDate?, month: YearMonth): Boolean {
        return if (beginningWeek != null) {
            if (month.month.value != beginningWeek.month.value) {
                false
            } else {
                val beginningWeekSelected = isDateInSelectedPeriod(beginningWeek)
                val lastDayPreviousWeek = beginningWeek.minusDays(1)
                isDateInSelectedPeriod(lastDayPreviousWeek) && beginningWeekSelected
            }
        } else {
            false
        }
    }

    fun isRightHighlighted(
        beginningWeek: LocalDate?,
        month: YearMonth
    ): Boolean {
        val lastDayOfTheWeek = beginningWeek?.plusDays(6)
        return if (lastDayOfTheWeek != null) {
            if (month.month.value != lastDayOfTheWeek.month.value) {
                false
            } else {
                val lastDayOfTheWeekSelected = isDateInSelectedPeriod(lastDayOfTheWeek)
                val firstDayNextWeek = lastDayOfTheWeek.plusDays(1)
                isDateInSelectedPeriod(firstDayNextWeek) && lastDayOfTheWeekSelected
            }
        } else {
            false
        }
    }

    fun dayDelay(currentWeekStartDate: LocalDate): Int {
        if (selectedStartDate == null && selectedEndDate == null) return 0
        // if selected week contains start date, don't have any delay
        val endWeek = currentWeekStartDate.plusDays(6)
        return if (selectedStartDate?.isBefore(currentWeekStartDate) == true ||
            selectedStartDate?.isAfter(endWeek) == true
        ) {
            // selected start date is not in current week
            abs(ChronoUnit.DAYS.between(currentWeekStartDate, selectedStartDate)).toInt()
        } else {
            0
        }
    }

    fun monthOverlapSelectionDelay(
        currentWeekStartDate: LocalDate,
        week: Week
    ): Int {
        val isStartInADifferentMonth = currentWeekStartDate.month != week.yearMonth.month

        return if (isStartInADifferentMonth) {
            var currentDate = currentWeekStartDate
            var offset = 0
            for (i in 0 until CalendarState.DAYS_IN_WEEK) {
                if (currentDate.month.value != week.yearMonth.month.value &&
                    isDateInSelectedPeriod(currentDate)
                ) {
                    offset++
                }
                currentDate = currentDate.plusDays(1)
            }
            offset
        } else {
            0
        }
    }

    fun setDates(newFrom: LocalDate?, newTo: LocalDate?): CalendarUiState {
        return if (newTo == null) {
            copy(selectedStartDate = newFrom)
        } else {
            copy(selectedStartDate = newFrom, selectedEndDate = newTo)
        }
    }

    fun setDate(new: LocalDate?): CalendarUiState {
        return copy(selectedStartDate = new)
    }
}
