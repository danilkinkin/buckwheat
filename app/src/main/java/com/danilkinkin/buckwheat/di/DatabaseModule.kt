package com.danilkinkin.buckwheat.di

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.danilkinkin.buckwheat.data.dao.SpentDao
import com.danilkinkin.buckwheat.data.dao.StorageDao
import com.danilkinkin.buckwheat.data.entities.Spent
import com.danilkinkin.buckwheat.data.entities.Storage

@Database(entities = [Spent::class, Storage::class], version = 1)
@TypeConverters(RoomConverters::class)
abstract class DatabaseModule : RoomDatabase() {
    abstract fun spentDao(): SpentDao

    abstract fun storageDao(): StorageDao
}
