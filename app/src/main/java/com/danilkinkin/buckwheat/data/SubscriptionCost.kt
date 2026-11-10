package com.danilkinkin.buckwheat.data

import com.danilkinkin.buckwheat.data.entities.Subscription
import com.danilkinkin.buckwheat.data.entities.SubscriptionPeriod
import com.danilkinkin.buckwheat.data.entities.toSubscriptionPeriod
import java.math.BigDecimal
import java.math.RoundingMode

/** Categories treated as bills/rent rather than service subscriptions in insights. */
val billCategoryNames = setOf("Home & Bills", "Mobile & Internet")

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
