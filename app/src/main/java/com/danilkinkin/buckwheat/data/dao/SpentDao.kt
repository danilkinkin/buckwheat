package com.danilkinkin.buckwheat.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.danilkinkin.buckwheat.data.entities.Spent

@Dao
interface SpentDao {
    @Query("SELECT * FROM spent")
    fun getAll(): LiveData<List<Spent>>

    @Insert
    fun insert(vararg spent: Spent)

    @Update
    fun update(vararg spent: Spent)

    @Delete
    fun delete(spent: Spent)

    @Query("DELETE FROM spent")
    fun deleteAll()
}
