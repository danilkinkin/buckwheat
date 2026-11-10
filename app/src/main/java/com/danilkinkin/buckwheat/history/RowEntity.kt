package com.danilkinkin.buckwheat.history

import com.danilkinkin.buckwheat.data.entities.Transaction
import java.math.BigDecimal
import java.time.LocalDate

enum class RowEntityType { DayDivider, Spent, DayTotal }

/**
 * One row of the month ledger. Day dividers carry the day total; spend rows carry their
 * transaction. Keys are stable so LazyColumn can reuse rows without the old DiffUtil animation
 * wrapper (which crashed when updates were applied out of order).
 */
data class RowEntity(
    val type: RowEntityType,
    val key: String,
    val day: LocalDate,
    val transaction: Transaction?,
    val dayTotal: BigDecimal?,
    val dayCount: Int? = null,
)
