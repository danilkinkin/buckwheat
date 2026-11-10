package com.danilkinkin.buckwheat.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date

/** One recorded amount change for a subscription, used by the price history insight. */
@Entity(tableName = "subscription_price_changes")
data class SubscriptionPriceChange(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subscriptionId: Int,
    val name: String,
    val oldAmount: BigDecimal,
    val newAmount: BigDecimal,
    val changedAt: Date,
)
