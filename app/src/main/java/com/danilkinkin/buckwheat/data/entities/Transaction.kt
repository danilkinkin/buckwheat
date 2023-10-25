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
) {
    @PrimaryKey(autoGenerate = true) var uid: Int = 0
}