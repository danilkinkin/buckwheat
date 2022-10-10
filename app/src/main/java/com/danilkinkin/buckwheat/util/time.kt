package com.danilkinkin.buckwheat.util

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.ceil

const val DAY = 24 * 60 * 60 * 1000

val monthFormat = SimpleDateFormat("MMMM")
val monthShortFormat = SimpleDateFormat("MM")
val yearFormat = SimpleDateFormat("yyyy")
val dateFormat = SimpleDateFormat("dd MMMM")
val timeFormat = SimpleDateFormat("HH:mm")

var yearMonthFormatterCurrYaer = DateTimeFormatter.ofPattern("MMMM")
var yearMonthFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

fun LocalDate.toDate(): Date = Date(this.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000)

fun Date.toLocalDate(): LocalDate = this.toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDate();

fun countDays(toDate: Date, fromDate: Date = Date()): Int {
    val fromDateRound = roundToDay(fromDate)
    val toDateRound = roundToDay(toDate)

    val daysFrom = ceil(fromDateRound.time / DAY.toDouble()).toInt()
    val daysTo = ceil(toDateRound.time / DAY.toDouble()).toInt()

    return daysTo - daysFrom
}

fun isSameDay(timestampA: Long, timestampB: Long): Boolean {
    return roundToDay(Date(timestampA)) == roundToDay(Date(timestampB))
}

fun prettyDate(
    date: Date,
    showTime: Boolean = true,
    forceShowDate: Boolean = false,
    forceHideDate: Boolean = false,
    forceShowYear: Boolean = false,
): String {
    val currentYear = yearFormat.format(Date().time)
    val currentDate = dateFormat.format(Date().time)

    val yearStr = yearFormat.format(date.time).toString()
    val dateStr = dateFormat.format(date.time).toString()
    val timeStr = timeFormat.format(date.time).toString()

    var final = ""

    if ((dateStr != currentDate || !showTime || forceShowDate) && !forceHideDate) {
        final += " $dateStr"
    }

    if (yearStr != currentYear || forceShowYear) {
        final += " $yearStr"
    }

    if (showTime) {
        final += " $timeStr"
    }

    return final
}

fun prettyYearMonth(yearMonth: YearMonth): String {
    return if (yearMonth.year.toString() == yearFormat.format(Date().time)) {
        yearMonth.format(yearMonthFormatterCurrYaer)
    } else {
        yearMonth.format(yearMonthFormatter)
    }
}

fun roundToDay(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date

    return Calendar
        .Builder()
        .setTimeZone(calendar.timeZone)
        .setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        .build()
        .time
}