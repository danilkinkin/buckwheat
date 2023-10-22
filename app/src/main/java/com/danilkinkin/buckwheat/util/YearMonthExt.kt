package com.danilkinkin.buckwheat.util

import android.content.Context
import java.time.YearMonth
import java.time.temporal.WeekFields


fun YearMonth.getNumberWeeks(context: Context): Int {
    val locale = context.resources.configuration.locales[0]

    val weekWithFixFirstWeekDay = WeekFields.of(WeekFields.of(locale).firstDayOfWeek, 1).weekOfWeekBasedYear()
    val weekNumberFirst = this.atDay(1).get(weekWithFixFirstWeekDay)
    val weekNumberLast = this.atEndOfMonth().get(weekWithFixFirstWeekDay)

    return weekNumberLast - weekNumberFirst + 1 // Both weeks inclusive
}
