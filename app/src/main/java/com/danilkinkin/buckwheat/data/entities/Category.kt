package com.danilkinkin.buckwheat.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    /** Legacy free-text emoji; kept so old rows and imports do not break. */
    val emoji: String = "",
    /** Key into [com.danilkinkin.buckwheat.base.categoryIconRes]; null falls back to Other. */
    val icon: String? = null,
    /** ARGB colour used for chips and charts. */
    val color: Long = 0xFF9E9E9E,
)
