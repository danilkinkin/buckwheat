package com.danilkinkin.buckwheat.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.danilkinkin.buckwheat.data.entities.Spent

@Dao
interface SpentDao {
    @Query("SELECT * FROM spent")
    fun getAll(): LiveData<List<Spent>>

    @Insert
    fun insert(vararg spent: Spent)

    @Delete
    fun delete(spent: Spent)

    @Query("DELETE FROM spent")
    fun deleteAll()
}
