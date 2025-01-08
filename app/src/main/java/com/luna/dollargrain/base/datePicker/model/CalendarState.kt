package com.luna.dollargrain.base.datePicker.model

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.luna.dollargrain.util.getNumberWeeks
import com.luna.dollargrain.util.toLocalDate
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import java.util.*

class CalendarState(
    context: Context,
    selectionMode: CalendarSelectionMode = CalendarSelectionMode.SINGLE,
    selectDate: Date? = null,
    disableBeforeDate: Date? = null,
    disableAfterDate: Date? = null,
) {

    val calendarUiState = mutableStateOf(
        CalendarUiState(
        selectionMode = selectionMode,
        disabledBefore = disableBeforeDate?.toLocalDate(),
        disabledAfter = disableAfterDate?.toLocalDate(),
    )
    )
    val listMonths: List<Month>

    private val calendarStartDate: LocalDate = LocalDate.now().withDayOfMonth(1)
    private val calendarEndDate: LocalDate = LocalDate.now().plusYears(2)
        .withMonth(12).withDayOfMonth(31)

    private val periodBetweenCalendarStartEnd: Period = Period.between(
        disableBeforeDate?.toLocalDate()?.withDayOfMonth(1) ?: calendarStartDate,
        disableAfterDate?.toLocalDate()?.withDayOfMonth(28) ?: calendarEndDate
    )

    init {
        val tempListMonths = mutableListOf<Month>()
        var startYearMonth = YearMonth.from(disableBeforeDate?.toLocalDate()?.withDayOfMonth(1) ?: calendarStartDate)

        for (numberMonth in 0..periodBetweenCalendarStartEnd.toTotalMonths()) {
            val numberWeeks = startYearMonth.getNumberWeeks(context)
            val listWeekItems = mutableListOf<Week>()
            for (week in 0 until numberWeeks) {
                listWeekItems.add(
                    Week(
                        number = week,
                        yearMonth = startYearMonth
                    )
                )
            }
            val month = Month(startYearMonth, listWeekItems)
            tempListMonths.add(month)
            startYearMonth = startYearMonth.plusMonths(1)
        }
        listMonths = tempListMonths.toList()

        if (selectDate != null) setSelectedDay(selectDate.toLocalDate())
    }

    fun setSelectedDay(newDate: LocalDate) {
        if (calendarUiState.value.selectionMode == CalendarSelectionMode.RANGE) {
            calendarUiState.value = calendarUiState.value.setDates(LocalDate.now(), newDate)
        } else {
            calendarUiState.value = calendarUiState.value.setDate(newDate)
        }
    }

    companion object {
        const val DAYS_IN_WEEK = 7
    }
}
