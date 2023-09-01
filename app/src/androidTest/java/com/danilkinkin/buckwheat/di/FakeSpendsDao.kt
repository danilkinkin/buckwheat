package com.danilkinkin.buckwheat.di

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.danilkinkin.buckwheat.data.dao.SpentDao
import com.danilkinkin.buckwheat.data.entities.Spent
import java.util.Date

class FakeSpendsDao: SpentDao {
    private val spends = mutableListOf<Spent>()

    override fun getAll(): LiveData<List<Spent>> {
        return MutableLiveData(spends)
    }

    override fun getAllSync(): List<Spent> {
        return spends
    }

    override fun getCountLastDaySpends(currDate: Date): LiveData<Int> {
        return MutableLiveData(0)
    }

    override fun getById(uid: Int): Spent? {
        return null
    }

    override fun insert(vararg spent: Spent) {
        spends.addAll(spent)
    }

    override fun update(vararg spent: Spent) {

    }

    override fun deleteById(uid: Int) {
        spends.removeIf { it.uid == uid }
    }

    override fun deleteAll() {

    }

}