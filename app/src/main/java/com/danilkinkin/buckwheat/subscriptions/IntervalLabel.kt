package com.danilkinkin.buckwheat.subscriptions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.danilkinkin.buckwheat.R
import com.danilkinkin.buckwheat.data.entities.SubscriptionPeriod

/** "3 months", "84 days", "1 year" - the interval as a human label. */
@Composable
fun subscriptionIntervalLabel(count: Int, period: SubscriptionPeriod): String {
    val safeCount = count.coerceAtLeast(1)

    return when (period) {
        SubscriptionPeriod.DAYS ->
            pluralStringResource(R.plurals.interval_days, safeCount, safeCount)

        SubscriptionPeriod.WEEKS ->
            pluralStringResource(R.plurals.interval_weeks, safeCount, safeCount)

        SubscriptionPeriod.MONTHS ->
            pluralStringResource(R.plurals.interval_months, safeCount, safeCount)

        SubscriptionPeriod.YEARS ->
            pluralStringResource(R.plurals.interval_years, safeCount, safeCount)
    }
}

@Composable
fun SubscriptionPeriod.unitLabel(): String = when (this) {
    SubscriptionPeriod.DAYS -> stringResource(R.string.interval_unit_days)
    SubscriptionPeriod.WEEKS -> stringResource(R.string.interval_unit_weeks)
    SubscriptionPeriod.MONTHS -> stringResource(R.string.interval_unit_months)
    SubscriptionPeriod.YEARS -> stringResource(R.string.interval_unit_years)
}
