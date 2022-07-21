package com.danilkinkin.buckwheat.utils

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

const val DAY = 24 * 60 * 60 * 1000

val yearFormat = SimpleDateFormat("yyyy")
val dateFormat = SimpleDateFormat("dd MMMM")
val timeFormat = SimpleDateFormat("HH:mm")

fun countDays(toDate: Date, fromDate: Date = Date()): Int {
    val daysFrom = ceil(fromDate.time / DAY.toDouble()).toInt()
    val daysTo = ceil(toDate.time / DAY.toDouble()).toInt()

    return daysTo - daysFrom
}

fun isSameDay(timestampA: Long, timestampB: Long): Boolean = floor(timestampA / DAY.toFloat()).toLong() == floor(timestampB / DAY.toFloat()).toLong()

fun prettyDate(date: Date, showTime: Boolean = true, forceShowDate: Boolean = false): String {
    val currentYear = yearFormat.format(Date().time)
    val currentDate = dateFormat.format(Date().time)

    val yearStr = yearFormat.format(date.time).toString()
    val dateStr = dateFormat.format(date.time).toString()
    val timeStr = timeFormat.format(date.time).toString()

    var final = ""

    if (dateStr != currentDate || !showTime || forceShowDate) {
        final += " $dateStr"
    }

    if (yearStr != currentYear) {
        final += " $yearStr"
    }

    if (showTime) {
        final += " $timeStr"
    }

    return final
}