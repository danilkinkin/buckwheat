package com.danilkinkin.buckwheat.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.danilkinkin.buckwheat.data.entities.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE ASC")
    fun getAll(): LiveData<List<Category>>

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getAllOnce(): List<Category>

    @Insert
    suspend fun insert(category: Category): Long

    @Insert
    suspend fun insertAll(categories: List<Category>)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    @Update
    suspend fun update(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Int)
}
