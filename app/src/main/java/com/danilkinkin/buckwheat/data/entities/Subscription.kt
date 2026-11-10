package com.danilkinkin.buckwheat.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date

/** Unit of a subscription interval; the count lives in [Subscription.intervalCount]. */
enum class SubscriptionPeriod { DAYS, WEEKS, MONTHS, YEARS }

/** Also accepts the pre-v9 WEEKLY/MONTHLY/YEARLY values. */
fun String.toSubscriptionPeriod(): SubscriptionPeriod = when (this) {
    "WEEKLY" -> SubscriptionPeriod.WEEKS
    "MONTHLY" -> SubscriptionPeriod.MONTHS
    "YEARLY" -> SubscriptionPeriod.YEARS
    else -> runCatching { SubscriptionPeriod.valueOf(this) }
        .getOrDefault(SubscriptionPeriod.MONTHS)
}

/**
 * Created in schema v6 even though the UI arrives later, so subscriptions do not need a second
 * migration once the ledger has real data in it.
 */
@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: BigDecimal,
    /** DAYS, WEEKS, MONTHS or YEARS. */
    val period: String,
    /** How many [period] units between renewals, e.g. 3 months or 84 days. */
    val intervalCount: Int = 1,
    /** Day of the month a MONTHLY subscription is anchored to (1-31). */
    val anchorDay: Int,
    val nextDue: Date,
    val categoryId: Int? = null,
    /** Optional entry from [com.danilkinkin.buckwheat.data.ServiceCatalog]. */
    val serviceId: String? = null,
    val active: Boolean = true,
)
