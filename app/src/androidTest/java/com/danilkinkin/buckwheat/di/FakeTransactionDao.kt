package com.danilkinkin.buckwheat.di

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.danilkinkin.buckwheat.data.dao.TransactionDao
import com.danilkinkin.buckwheat.data.entities.Transaction
import com.danilkinkin.buckwheat.data.entities.TransactionType

class FakeTransactionDao: TransactionDao {
    private val spends = mutableListOf<Transaction>()

    override fun getAll(): LiveData<List<Transaction>> {
        return MutableLiveData(spends)
    }

    override fun getAll(type: TransactionType): LiveData<List<Transaction>> {
        return MutableLiveData(spends)
    }

    override fun getById(uid: Int): Transaction? {
        return null
    }

    override fun insert(vararg transaction: Transaction) {
        spends.addAll(transaction)
    }

    override fun update(vararg transaction: Transaction) {

    }

    override fun deleteById(uid: Int) {
        spends.removeIf { it.uid == uid }
    }

    override fun deleteAll() {

    }

}