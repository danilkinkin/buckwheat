package com.danilkinkin.buckwheat.data

import com.danilkinkin.buckwheat.data.entities.SubscriptionPeriod
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SubscriptionScheduleTest {

    @Test
    fun `custom days advance by days`() {
        assertEquals(
            LocalDate.of(2026, 3, 26),
            nextSubscriptionDate(
                period = SubscriptionPeriod.DAYS,
                intervalCount = 84,
                anchorDay = 1,
                from = LocalDate.of(2026, 1, 1),
            ),
        )
    }

    @Test
    fun `weeks advance by weeks`() {
        assertEquals(
            LocalDate.of(2026, 1, 29),
            nextSubscriptionDate(
                period = SubscriptionPeriod.WEEKS,
                intervalCount = 4,
                anchorDay = 1,
                from = LocalDate.of(2026, 1, 1),
            ),
        )
    }

    @Test
    fun `months clamp to the target month`() {
        assertEquals(
            LocalDate.of(2026, 2, 28),
            nextSubscriptionDate(
                period = SubscriptionPeriod.MONTHS,
                intervalCount = 1,
                anchorDay = 31,
                from = LocalDate.of(2026, 1, 31),
            ),
        )
    }

    @Test
    fun `months restore the anchor after a clamped month`() {
        assertEquals(
            LocalDate.of(2026, 3, 31),
            nextSubscriptionDate(
                period = SubscriptionPeriod.MONTHS,
                intervalCount = 1,
                anchorDay = 31,
                from = LocalDate.of(2026, 2, 28),
            ),
        )
    }

    @Test
    fun `three month plan lands on the last valid day`() {
        assertEquals(
            LocalDate.of(2026, 4, 30),
            nextSubscriptionDate(
                period = SubscriptionPeriod.MONTHS,
                intervalCount = 3,
                anchorDay = 31,
                from = LocalDate.of(2026, 1, 31),
            ),
        )
    }

    @Test
    fun `years advance by years`() {
        assertEquals(
            LocalDate.of(2028, 2, 29),
            nextSubscriptionDate(
                period = SubscriptionPeriod.YEARS,
                intervalCount = 2,
                anchorDay = 29,
                from = LocalDate.of(2026, 2, 28),
            ),
        )
    }
}
