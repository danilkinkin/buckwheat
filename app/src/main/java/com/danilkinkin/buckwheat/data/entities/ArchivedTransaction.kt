package com.danilkinkin.buckwheat.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.*

@Entity(
    tableName = "archived_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Period::class,
            parentColumns = ["uid"],
            childColumns = ["period_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ArchivedTransaction(
    @PrimaryKey(autoGenerate = true)
    val uid: Int = 0,

    @ColumnInfo(name = "period_id", index = true)
    val periodId: Int,

    @ColumnInfo(name = "type")
    val type: TransactionType,

    @ColumnInfo(name = "value")
    val value: BigDecimal,

    @ColumnInfo(name = "date")
    val date: Date,

    @ColumnInfo(name = "comment", defaultValue = "")
    val comment: String = ""
)
