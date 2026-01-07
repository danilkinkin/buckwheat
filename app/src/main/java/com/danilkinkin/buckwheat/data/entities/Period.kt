package com.danilkinkin.buckwheat.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.*

@Entity(tableName = "periods")
data class Period(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,

    @ColumnInfo(name = "budget")
    val budget: BigDecimal,

    @ColumnInfo(name = "spent")
    val spent: BigDecimal,

    @ColumnInfo(name = "currency")
    val currency: String,

    @ColumnInfo(name = "start_date")
    val startDate: Date,

    @ColumnInfo(name = "finish_date")
    val finishDate: Date,

    @ColumnInfo(name = "actual_finish_date")
    val actualFinishDate: Date,

    @ColumnInfo(name = "archived_at")
    val archivedAt: Date = Date()
)
