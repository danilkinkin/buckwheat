package com.danilkinkin.buckwheat.di

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.danilkinkin.buckwheat.data.dao.ArchivedTransactionDao
import com.danilkinkin.buckwheat.data.dao.PeriodDao
import com.danilkinkin.buckwheat.data.dao.TransactionDao
import com.danilkinkin.buckwheat.data.entities.ArchivedTransaction
import com.danilkinkin.buckwheat.data.entities.Period
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val periodDao: PeriodDao,
    private val archivedTransactionDao: ArchivedTransactionDao,
    private val transactionDao: TransactionDao,
) {
    fun getAllPeriods(): LiveData<List<Period>> = periodDao.getAll()

    fun getPeriodById(periodId: Int): LiveData<Period?> = periodDao.getById(periodId)

    fun getTransactionsForPeriod(periodId: Int): LiveData<List<ArchivedTransaction>> =
        archivedTransactionDao.getByPeriodId(periodId)

    suspend fun archiveCurrentPeriod(
        budget: BigDecimal,
        spent: BigDecimal,
        currency: String,
        startDate: Date,
        finishDate: Date,
        actualFinishDate: Date,
    ) {
        // Get all current transactions
        val currentTransactions = transactionDao.getAllSync()

        // Only archive if there are spends (not just the income transaction)
        val hasSpends = currentTransactions.any { it.type == TransactionType.SPENT }
        if (!hasSpends) {
            Log.d("HistoryRepository", "No spends to archive, skipping")
            return
        }

        // Calculate actual spent from transactions (more accurate than stored value)
        val calculatedSpent = currentTransactions
            .filter { it.type == TransactionType.SPENT }
            .fold(BigDecimal.ZERO) { acc, transaction -> acc + transaction.value }

        // Create the period record
        val period = Period(
            budget = budget,
            spent = calculatedSpent,
            currency = currency,
            startDate = startDate,
            finishDate = finishDate,
            actualFinishDate = actualFinishDate,
            archivedAt = Date()
        )

        val periodId = periodDao.insert(period).toInt()

        Log.d(
            "HistoryRepository",
            "Archived period ["
                    + "periodId: $periodId "
                    + "budget: $budget "
                    + "spent: $calculatedSpent "
                    + "startDate: $startDate "
                    + "finishDate: $finishDate "
                    + "]"
        )

        // Archive all transactions
        val archivedTransactions = currentTransactions.map { transaction ->
            ArchivedTransaction(
                periodId = periodId,
                type = transaction.type,
                value = transaction.value,
                date = transaction.date,
                comment = transaction.comment
            )
        }

        archivedTransactionDao.insertAll(archivedTransactions)

        Log.d(
            "HistoryRepository",
            "Archived ${archivedTransactions.size} transactions for period $periodId"
        )
    }

    suspend fun deletePeriod(periodId: Int) {
        // Foreign key cascade will delete archived transactions
        periodDao.deleteById(periodId)
    }

    suspend fun deleteAllHistory() {
        periodDao.deleteAll()
    }
}
