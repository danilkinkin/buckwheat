package com.danilkinkin.buckwheat.history

import com.danilkinkin.buckwheat.data.entities.Transaction

/**
 * Pure search predicate used by the All Expenses filter. Kept outside the composable so it can be
 * unit tested without Compose or Android resources.
 */
fun transactionMatchesQuery(
    transaction: Transaction,
    query: String,
    categoryName: String?,
    serviceName: String?,
): Boolean {
    val needle = query.trim().lowercase()
    if (needle.isEmpty()) return true

    return transaction.comment.lowercase().contains(needle) ||
            categoryName?.lowercase()?.contains(needle) == true ||
            serviceName?.lowercase()?.contains(needle) == true ||
            transaction.value.toPlainString().contains(needle)
}
