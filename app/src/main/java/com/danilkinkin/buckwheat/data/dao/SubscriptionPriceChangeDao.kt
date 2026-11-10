package com.danilkinkin.buckwheat.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.danilkinkin.buckwheat.data.entities.SubscriptionPriceChange

@Dao
interface SubscriptionPriceChangeDao {
    @Query("SELECT * FROM subscription_price_changes ORDER BY changedAt DESC")
    fun getAll(): LiveData<List<SubscriptionPriceChange>>

    @Query("SELECT * FROM subscription_price_changes ORDER BY changedAt DESC")
    suspend fun getAllOnce(): List<SubscriptionPriceChange>

    @Insert
    suspend fun insert(change: SubscriptionPriceChange)

    @Insert
    suspend fun insertAll(changes: List<SubscriptionPriceChange>)

    @Query("DELETE FROM subscription_price_changes")
    suspend fun deleteAll()

    @Query("DELETE FROM subscription_price_changes WHERE subscriptionId = :subscriptionId")
    suspend fun deleteForSubscription(subscriptionId: Int)
}
