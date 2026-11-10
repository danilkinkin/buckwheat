package com.danilkinkin.buckwheat.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.danilkinkin.buckwheat.data.entities.Subscription

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions ORDER BY active DESC, nextDue ASC, name COLLATE NOCASE ASC")
    fun getAll(): LiveData<List<Subscription>>

    @Query("SELECT * FROM subscriptions WHERE active = 1 AND nextDue <= :time ORDER BY nextDue ASC")
    suspend fun getDue(time: Long): List<Subscription>

    @Query("SELECT * FROM subscriptions ORDER BY id ASC")
    suspend fun getAllOnce(): List<Subscription>

    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getById(id: Int): Subscription?

    @Insert
    suspend fun insertAll(subscriptions: List<Subscription>)

    @Query("DELETE FROM subscriptions")
    suspend fun deleteAll()

    @Insert
    suspend fun insert(subscription: Subscription): Long

    @Update
    suspend fun update(subscription: Subscription)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteById(id: Int)
}
