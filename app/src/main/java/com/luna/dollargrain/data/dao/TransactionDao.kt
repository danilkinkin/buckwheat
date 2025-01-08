package com.luna.dollargrain.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.luna.dollargrain.data.entities.Transaction
import com.luna.dollargrain.data.entities.TransactionType

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date ASC")
    fun getAll(): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date ASC")
    fun getAll(type: TransactionType): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE uid = :uid")
    fun getById(uid: Int): Transaction?

    @Insert
    fun insert(vararg transaction: Transaction)

    @Update(entity = Transaction::class, onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg transaction: Transaction)

    @Query("DELETE FROM transactions WHERE uid = :uid")
    fun deleteById(uid: Int)

    @Query("DELETE FROM transactions")
    fun deleteAll()
}
