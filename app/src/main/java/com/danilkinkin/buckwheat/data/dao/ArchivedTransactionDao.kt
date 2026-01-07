package com.danilkinkin.buckwheat.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.danilkinkin.buckwheat.data.entities.ArchivedTransaction

@Dao
interface ArchivedTransactionDao {
    @Query("SELECT * FROM archived_transactions WHERE period_id = :periodId ORDER BY date ASC")
    fun getByPeriodId(periodId: Int): LiveData<List<ArchivedTransaction>>

    @Query("SELECT * FROM archived_transactions WHERE period_id = :periodId ORDER BY date ASC")
    suspend fun getByPeriodIdSync(periodId: Int): List<ArchivedTransaction>

    @Insert
    suspend fun insert(transaction: ArchivedTransaction)

    @Insert
    suspend fun insertAll(transactions: List<ArchivedTransaction>)

    @Query("DELETE FROM archived_transactions WHERE period_id = :periodId")
    suspend fun deleteByPeriodId(periodId: Int)

    @Query("DELETE FROM archived_transactions")
    suspend fun deleteAll()
}
