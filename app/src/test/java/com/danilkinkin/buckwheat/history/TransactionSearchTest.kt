package com.danilkinkin.buckwheat.history

import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.Date

class TransactionSearchTest {

    private val transaction = Transaction(
        type = TransactionType.SPENT,
        value = BigDecimal("149"),
        date = Date(),
        comment = "Netflix subscription",
    )

    @Test
    fun `matches note case-insensitively`() {
        assertTrue(transactionMatchesQuery(transaction, "netflix", null, null))
    }

    @Test
    fun `matches category and service names`() {
        assertTrue(transactionMatchesQuery(transaction, "stream", "Streaming", null))
        assertTrue(transactionMatchesQuery(transaction, "net", null, "Netflix"))
    }

    @Test
    fun `matches amount`() {
        assertTrue(transactionMatchesQuery(transaction, "149", null, null))
    }

    @Test
    fun `empty query matches everything`() {
        assertTrue(transactionMatchesQuery(transaction, "  ", null, null))
    }

    @Test
    fun `unrelated query does not match`() {
        assertFalse(transactionMatchesQuery(transaction, "groceries", "Food", null))
    }
}
