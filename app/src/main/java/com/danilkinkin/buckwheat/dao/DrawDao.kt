package com.danilkinkin.buckwheat.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.danilkinkin.buckwheat.entities.Draw

@Dao
interface DrawDao {
    @Query("SELECT * FROM draw")
    fun getAll(): LiveData<List<Draw>>

    @Insert
    fun insert(vararg draw: Draw)

    @Delete
    fun delete(draw: Draw)

    @Query("DELETE FROM draw")
    fun deleteAll()
}
