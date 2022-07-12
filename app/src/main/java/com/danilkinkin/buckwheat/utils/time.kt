package com.danilkinkin.buckwheat.utils

import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

const val DAY = 24 * 60 * 60 * 1000

fun countDays(toDate: Date, fromDate: Date = Date()): Int = ceil((toDate.time - fromDate.time) / DAY.toFloat()).toInt()

fun isSameDay(timestampA: Long, timestampB: Long): Boolean = floor(timestampA / DAY.toFloat()).toLong() == floor(timestampB / DAY.toFloat()).toLong()