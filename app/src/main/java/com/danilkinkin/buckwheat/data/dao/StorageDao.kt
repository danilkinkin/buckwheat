package com.danilkinkin.buckwheat.data.dao

import androidx.room.*
import com.danilkinkin.buckwheat.data.entities.Storage
import java.math.BigDecimal
import java.util.Date

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