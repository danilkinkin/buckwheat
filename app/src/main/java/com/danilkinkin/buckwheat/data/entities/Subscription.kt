package com.danilkinkin.buckwheat.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date

enum class SubscriptionPeriod { WEEKLY, MONTHLY, YEARLY }

fun String.toSubscriptionPeriod(): SubscriptionPeriod =
    runCatching { SubscriptionPeriod.valueOf(this) }.getOrDefault(SubscriptionPeriod.MONTHLY)

/**
 * Created in schema v6 even though the UI arrives later, so subscriptions do not need a second
 * migration once the ledger has real data in it.
 */
@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: BigDecimal,
    /** WEEKLY, MONTHLY or YEARLY. */
    val period: String,
    /** Day of the month a MONTHLY subscription is anchored to (1-31). */
    val anchorDay: Int,
    val nextDue: Date,
    val categoryId: Int? = null,
    /** Optional entry from [com.danilkinkin.buckwheat.data.ServiceCatalog]. */
    val serviceId: String? = null,
    val active: Boolean = true,
)
