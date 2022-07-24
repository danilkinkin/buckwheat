package com.danilkinkin.buckwheat.di

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.danilkinkin.buckwheat.dao.SpentDao
import com.danilkinkin.buckwheat.dao.StorageDao
import com.danilkinkin.buckwheat.entities.Spent
import com.danilkinkin.buckwheat.entities.Storage


lateinit var instanceDB: DatabaseModule

@Database(entities = [Spent::class, Storage::class], version = 1)
@TypeConverters(RoomConverters::class)
abstract class DatabaseModule : RoomDatabase() {

    companion object {
        fun getInstance(applicationContext: Application): DatabaseModule {
            if (::instanceDB.isInitialized) {
                return instanceDB
            }

            instanceDB = Room.databaseBuilder(
                applicationContext,
                DatabaseModule::class.java,
                "buckwheat-db",
            )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()


            return instanceDB
        }
    }

    abstract fun spentDao(): SpentDao

    abstract fun storageDao(): StorageDao
}
