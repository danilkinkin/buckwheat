package com.danilkinkin.buckwheat.data

import com.danilkinkin.buckwheat.data.entities.SubscriptionPeriod
import java.time.LocalDate

/**
 * Next occurrence of a subscription, always after [from].
 *
 * Monthly anchors day-of-month and clamps to the target month's length, so a subscription on the
 * 31st lands on 28/29 February. Yearly uses [LocalDate.plusYears], which already clamps 29 Feb.
 */
fun nextSubscriptionDate(
    period: SubscriptionPeriod,
    anchorDay: Int,
    from: LocalDate,
): LocalDate = when (period) {
    SubscriptionPeriod.WEEKLY -> from.plusWeeks(1)

    SubscriptionPeriod.MONTHLY -> {
        val nextMonth = from.plusMonths(1)
        nextMonth.withDayOfMonth(anchorDay.coerceIn(1, nextMonth.lengthOfMonth()))
    }

    SubscriptionPeriod.YEARLY -> from.plusYears(1)
}
