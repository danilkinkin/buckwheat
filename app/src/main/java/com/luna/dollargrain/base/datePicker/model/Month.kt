package com.luna.dollargrain.base.datePicker.model

import java.time.YearMonth

data class Month(
    val yearMonth: YearMonth,
    val weeks: List<Week>
)
