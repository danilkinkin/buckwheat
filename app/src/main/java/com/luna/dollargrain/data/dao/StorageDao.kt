package com.luna.dollargrain.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.luna.dollargrain.data.entities.Storage

@Dao
interface StorageDao {
    @Query("SELECT * FROM storage WHERE name = :name")
    fun get(name: String): Storage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun set(vararg storage: Storage)

    @Delete
    fun delete(storage: Storage)

    @Query("DELETE FROM storage")
    fun deleteAll()
}