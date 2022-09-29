package com.danilkinkin.buckwheat.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.danilkinkin.buckwheat.data.entities.Spent
import com.danilkinkin.buckwheat.util.roundToDay
import java.util.*

@Dao
interface SpentDao {
    @Query("SELECT * FROM spent ORDER BY date ASC")
    fun getAll(): LiveData<List<Spent>>

    @Query("SELECT COUNT(*) FROM spent WHERE date > :currDate AND deleted = false ORDER BY date ASC")
    fun getCountLastDaySpends(currDate: Date = roundToDay(Date())): LiveData<Int>

    @Query("SELECT * FROM spent WHERE uid = :uid")
    fun getById(uid: Int): Spent?

    @Insert
    fun insert(vararg spent: Spent)

    @Update(entity = Spent::class, onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg spent: Spent)

    @Query("UPDATE spent SET deleted = :deleted WHERE uid = :uid")
    fun markAsDeleted(uid: Int, deleted: Boolean)

    @Query("DELETE FROM spent WHERE deleted = true")
    fun commitDeleted()

    @Delete
    fun delete(spent: Spent)

    @Query("DELETE FROM spent")
    fun deleteAll()
}
