package com.danilkinkin.buckwheat.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.*

enum class TransactionType {
    SET_DAILY_BUDGET,
    INCOME,
    SPENT
}

@Entity(tableName = "transactions")
data class Transaction(
    @ColumnInfo(name = "type")
    val type: TransactionType,

    @ColumnInfo(name = "value")
    val value: BigDecimal,

    @ColumnInfo(name = "date")
    val date: Date,

    @ColumnInfo(name = "comment", defaultValue = "")
    val comment: String = "",

    // Nullable on purpose: an expense without a category is a valid, visible state
    // ("Uncategorised"), and it is what every pre-v6 row becomes.
    @ColumnInfo(name = "categoryId", index = true)
    val categoryId: Int? = null,
) {
    @PrimaryKey(autoGenerate = true) var uid: Int = 0
}