package com.danilkinkin.buckwheat.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.ceil

const val DAY = 24 * 60 * 60 * 1000

val yearOnlyFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy")
val monthWithYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("LLLL yyyy")
val monthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("LLLL")
val dateWithMonthFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM")
val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun LocalDate.toDate(): Date = Date(this.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000)

fun Date.toLocalDate(): LocalDate = this.toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDate()

fun Date.toLocalDateTime(): LocalDateTime = this.toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDateTime()

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
    val currentYear = yearOnlyFormatter.format(LocalDate.now())
    val currentDate = dateWithMonthFormatter.format(LocalDate.now())

    val convertedYear = yearOnlyFormatter.format(date.toLocalDate())
    val convertedDate = dateWithMonthFormatter.format(date.toLocalDate())
    val convertedTime = timeFormatter.format(date.toLocalDateTime())

    var final = ""

    if ((convertedDate != currentDate || !showTime || forceShowDate) && !forceHideDate) {
        final += " $convertedDate"
    }

    if (convertedYear != currentYear || forceShowYear) {
        final += " $convertedYear"
    }

    if (showTime) {
        final += " $convertedTime"
    }

    return final
}

fun prettyYearMonth(yearMonth: YearMonth): String {
    return if (yearMonth.year.toString() == yearOnlyFormatter.format(LocalDate.now())) {
        yearMonth.format(monthFormatter)
    } else {
        yearMonth.format(monthWithYearFormatter)
    }.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
        ) else it.toString()
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