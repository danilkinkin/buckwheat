package com.danilkinkin.buckwheat.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.danilkinkin.buckwheat.entities.Draw

@Dao
interface DrawDao {
    @Query("SELECT * FROM draw")
    fun getAll(): List<Draw>

    @Insert
    fun insertAll(vararg draw: Draw)

    @Delete
    fun delete(draw: Draw)
}
