package com.danilkinkin.buckwheat.data

import com.danilkinkin.buckwheat.data.entities.Subscription
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.util.Date

class SubscriptionCostTest {

    private fun subscription(amount: String, period: String, count: Int) = Subscription(
        id = 1,
        name = "test",
        amount = BigDecimal(amount),
        period = period,
        intervalCount = count,
        anchorDay = 1,
        nextDue = Date(0),
    )

    @Test
    fun `three month plan normalises to one month`() {
        assertEquals(
            BigDecimal("100.00"),
            monthlyEquivalent(subscription("300", "MONTHS", 3)),
        )
    }

    @Test
    fun `yearly plan normalises to monthly`() {
        assertEquals(
            BigDecimal("83.25"),
            monthlyEquivalent(subscription("999", "YEARS", 1)),
        )
    }

    @Test
    fun `84 day plan normalises to monthly`() {
        assertEquals(
            BigDecimal("304.38"),
            monthlyEquivalent(subscription("840", "DAYS", 84)),
        )
    }

    @Test
    fun `two week plan normalises to monthly`() {
        assertEquals(
            BigDecimal("217.25"),
            monthlyEquivalent(subscription("100", "WEEKS", 2)),
        )
    }
}
