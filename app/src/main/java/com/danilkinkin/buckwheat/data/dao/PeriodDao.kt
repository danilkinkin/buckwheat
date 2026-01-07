package com.danilkinkin.buckwheat.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.danilkinkin.buckwheat.data.entities.Period

@Dao
interface PeriodDao {
    @Query("SELECT * FROM periods ORDER BY archived_at DESC")
    fun getAll(): LiveData<List<Period>>

    @Query("SELECT * FROM periods ORDER BY archived_at DESC")
    suspend fun getAllSync(): List<Period>

    @Query("SELECT * FROM periods WHERE uid = :uid")
    fun getById(uid: Int): LiveData<Period?>

    @Query("SELECT * FROM periods WHERE uid = :uid")
    suspend fun getByIdSync(uid: Int): Period?

    @Insert
    suspend fun insert(period: Period): Long

    @Update
    suspend fun update(period: Period)

    @Query("DELETE FROM periods WHERE uid = :uid")
    suspend fun deleteById(uid: Int)

    @Query("DELETE FROM periods")
    suspend fun deleteAll()
}
