package com.danilkinkin.buckwheat.util

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.danilkinkin.buckwheat.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Date
import kotlin.math.ceil

const val DAY = 24 * 60 * 60 * 1000

fun LocalDate.toDate(): Date = Date(this.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000)

fun LocalDateTime.toDate(): Date = Date(this.toEpochSecond(
    ZoneId.systemDefault().rules.getOffset(this)
) * 1000)

fun Date.toLocalDate(): LocalDate = this.toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDate()

fun Date.toLocalDateTime(): LocalDateTime = this.toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDateTime()

fun countDaysToToday(fromDate: Date): Int {
    return countDays(fromDate, Date())
}

fun countDays(toDate: Date, fromDate: Date): Int {
    val fromDateRound = roundToDay(fromDate)
    val toDateRound = roundToDay(toDate)

    val daysFrom = ceil(fromDateRound.time / DAY.toDouble()).toInt()
    val daysTo = ceil(toDateRound.time / DAY.toDouble()).toInt()

    return daysTo - daysFrom + 1
}

fun isToday(date: Date): Boolean {
    return isSameDay(date.time, Date().time)
}

fun isSameDay(timestampA: Long, timestampB: Long): Boolean {
    return isSameDay(Date(timestampA), Date(timestampB))
}

fun isSameDay(dateA: Date, dateB: Date): Boolean {
    return roundToDay(dateA) == roundToDay(dateB)
}
@Composable
fun getWeek(): Array<DayOfWeek> {
    val locale = LocalConfiguration.current.locales[0]

    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek

    return if (firstDayOfWeek == DayOfWeek.MONDAY) {
        arrayOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    } else {
        arrayOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
    }
}

@Composable
fun prettyDate(
    date: Date,
    pattern: String,
    simplifyIfToday: Boolean = true,
): String {
    val locale = LocalConfiguration.current.locales[0]

    val dateWithMonthAndYearFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", locale)
    val currentFullDate = dateWithMonthAndYearFormatter.format(LocalDate.now())
    val convertedFullDate = dateWithMonthAndYearFormatter.format(date.toLocalDateTime())

    var final = ""

    final += if (simplifyIfToday && convertedFullDate == currentFullDate) {
        stringResource(R.string.today)
    } else {
        DateTimeFormatter.ofPattern(pattern, locale).format(date.toLocalDateTime())
    }

    return final.trim()
}

@Composable
fun prettyDate(
    date: Date,
    showTime: Boolean = true,
    forceShowDate: Boolean = false,
    forceHideDate: Boolean = false,
    forceShowYear: Boolean = false,
    human: Boolean = false,
    shortMonth: Boolean = false,
): String {
    val locale = LocalConfiguration.current.locales[0]

    val yearOnlyFormatter = DateTimeFormatter.ofPattern("yyyy", locale)
    val dateWithMonthAndYearFormatter = if (shortMonth) {
        DateTimeFormatter.ofPattern("dd MMM yyyy", locale)
    } else {
        DateTimeFormatter.ofPattern("dd MMMM yyyy", locale)
    }
    val dateWithMonthFormatter = if (shortMonth) {
        DateTimeFormatter.ofPattern("dd MMM", locale)
    } else {
        DateTimeFormatter.ofPattern("dd MMMM", locale)
    }
    val timeFormatter = if (DateFormat.is24HourFormat(LocalContext.current)) {
        DateTimeFormatter.ofPattern("HH:mm", locale)
    } else {
        DateTimeFormatter.ofPattern("KK:mm", locale)
    }


    val currentFullDate = dateWithMonthAndYearFormatter.format(LocalDate.now())
    val currentYear = yearOnlyFormatter.format(LocalDate.now())

    val convertedFullDate = dateWithMonthAndYearFormatter.format(date.toLocalDate())
    val convertedYear = yearOnlyFormatter.format(date.toLocalDate())
    val convertedDate = dateWithMonthFormatter.format(date.toLocalDate())
    val convertedTime = timeFormatter.format(date.toLocalDateTime())

    var final = ""

    if (human && convertedFullDate == currentFullDate) {
        final += stringResource(R.string.today)
    }

    if (!human || convertedFullDate != currentFullDate) {
        if ((convertedFullDate != currentFullDate || !showTime || forceShowDate) && !forceHideDate) {
            final += convertedDate
        }

        if (convertedYear != currentYear || forceShowYear) {
            final += " $convertedYear"
        }
    }

    if (showTime) {
        final += " $convertedTime"
    }

    return final.trim()
}

@Composable
fun prettyYearMonth(yearMonth: YearMonth): String {
    val locale = LocalConfiguration.current.locales[0]

    val yearOnlyFormatter = DateTimeFormatter.ofPattern("yyyy", locale)
    val monthFormatter = DateTimeFormatter.ofPattern("LLLL", locale)
    val monthWithYearFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", locale)

    return if (yearMonth.year.toString() == yearOnlyFormatter.format(LocalDate.now())) {
        yearMonth.format(monthFormatter)
    } else {
        yearMonth.format(monthWithYearFormatter)
    }.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            locale
        ) else it.toString()
    }
}

@Composable
fun prettyWeekDay(dayOfWeek: DayOfWeek): String {
    val locale = LocalConfiguration.current.locales[0]

    val dayOfWeekFormatter = if (locale.language == "ru") {
        DateTimeFormatter.ofPattern("ccc", locale)
    } else {
        DateTimeFormatter.ofPattern("ccccc", locale)
    }

    return dayOfWeekFormatter.format(dayOfWeek).uppercase(locale)
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