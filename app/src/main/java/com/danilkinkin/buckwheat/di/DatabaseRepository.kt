package com.danilkinkin.buckwheat.di

import com.danilkinkin.buckwheat.data.dao.SpentDao
import com.danilkinkin.buckwheat.data.dao.StorageDao
import javax.inject.Inject

class DatabaseRepository @Inject constructor(
    private val spentDao: SpentDao,
    private val storageDao: StorageDao
){
    fun spentDao() = spentDao
    fun storageDao() = storageDao
}