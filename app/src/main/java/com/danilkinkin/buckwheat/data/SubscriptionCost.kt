package com.danilkinkin.buckwheat.data

import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.entities.SubscriptionPeriod
import com.danilkinkin.buckwheat.data.entities.toSubscriptionPeriod
import java.math.BigDecimal
import java.math.RoundingMode

/** Categories treated as bills/rent rather than service subscriptions in insights. */
val billCategoryNames = setOf("Home & Bills", "Mobile & Internet")

/** Percentage change, or null when there is no previous amount to compare with. */
fun percentDelta(current: BigDecimal, previous: BigDecimal): Int? {
    if (previous <= BigDecimal.ZERO) return null

    return current
        .subtract(previous)
        .multiply(BigDecimal(100))
        .divide(previous, 0, RoundingMode.HALF_UP)
        .toInt()
}

/** The monthly share of one subscription, normalised so mixed intervals can be summed. */
fun monthlyEquivalent(subscription: Subscription): BigDecimal {
    val count = subscription.intervalCount.coerceAtLeast(1).toBigDecimal()

    return when (subscription.period.toSubscriptionPeriod()) {
        SubscriptionPeriod.DAYS -> subscription.amount
            .multiply(BigDecimal("30.4375"))
            .divide(count, 2, RoundingMode.HALF_UP)

        SubscriptionPeriod.WEEKS -> subscription.amount
            .multiply(BigDecimal("4.345"))
            .divide(count, 2, RoundingMode.HALF_UP)

        SubscriptionPeriod.MONTHS -> subscription.amount
            .divide(count, 2, RoundingMode.HALF_UP)

        SubscriptionPeriod.YEARS -> subscription.amount
            .divide(count.multiply(BigDecimal(12)), 2, RoundingMode.HALF_UP)
    }
}
