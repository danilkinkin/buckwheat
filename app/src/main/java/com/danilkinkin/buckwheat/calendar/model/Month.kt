package com.danilkinkin.buckwheat.calendar.model

import java.time.YearMonth

data class Month(
    val yearMonth: YearMonth,
    val weeks: List<Week>
)
