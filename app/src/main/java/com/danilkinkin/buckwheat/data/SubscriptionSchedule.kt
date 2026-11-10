package com.danilkinkin.buckwheat.data

import com.danilkinkin.buckwheat.data.entities.SubscriptionPeriod
import java.time.LocalDate
import java.time.YearMonth

/**
 * Next occurrence of a subscription, always after [from].
 *
 * Monthly anchors day-of-month and clamps to the target month's length, so a subscription on the
 * 31st lands on 28/29 February. Yearly uses [LocalDate.plusYears], which already clamps 29 Feb.
 */
fun nextSubscriptionDate(
    period: SubscriptionPeriod,
    intervalCount: Int,
    anchorDay: Int,
    from: LocalDate,
): LocalDate {
    val count = intervalCount.coerceAtLeast(1).toLong()

    return when (period) {
        SubscriptionPeriod.DAYS -> from.plusDays(count)
        SubscriptionPeriod.WEEKS -> from.plusWeeks(count)

        SubscriptionPeriod.MONTHS -> {
            val target = YearMonth.from(from).plusMonths(count)
            target.atDay(anchorDay.coerceIn(1, target.lengthOfMonth()))
        }

        SubscriptionPeriod.YEARS -> from.plusYears(count)
    }
}
