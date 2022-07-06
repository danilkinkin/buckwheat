package com.danilkinkin.buckwheat.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Storage(
    @PrimaryKey
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "value")
    val value: String,
)