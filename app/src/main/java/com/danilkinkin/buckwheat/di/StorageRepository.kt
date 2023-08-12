package com.danilkinkin.buckwheat.di

import com.danilkinkin.buckwheat.data.dao.SpentDao
import com.danilkinkin.buckwheat.data.dao.StorageDao
import javax.inject.Inject

class StorageRepository @Inject constructor(
    private val storageDao: StorageDao
){
    fun storageDao() = storageDao


}