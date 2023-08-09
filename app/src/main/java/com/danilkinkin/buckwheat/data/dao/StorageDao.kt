package com.danilkinkin.buckwheat.data.dao

import androidx.room.*
import com.danilkinkin.buckwheat.data.entities.Storage
import java.math.BigDecimal
import java.util.Date

@Dao
interface StorageDao {
    @Query("SELECT * FROM storage WHERE name = :name")
    fun get(name: String): Storage

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun set(vararg storage: Storage)

    @Delete
    fun delete(storage: Storage)

    fun getAsBigDecimal(name: String, defaultValue: BigDecimal): BigDecimal {
        return try {
            this.get(name).value.toBigDecimal()
        } catch (e: Exception) {
            defaultValue
        }
    }
    fun getAsBoolean(name: String, defaultValue: Boolean): Boolean {
        return try {
            this.get(name).value.toBoolean()
        } catch (e: Exception) {
            defaultValue
        }
    }
    fun getAsDate(name: String, defaultValue: Date?): Date? {
        return try {
            Date(this.get(name).value.toLong())
        } catch (e: Exception) {
            defaultValue
        }
    }
}