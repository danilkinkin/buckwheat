package com.danilkinkin.buckwheat.di

import android.app.Application
import androidx.annotation.NonNull
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.danilkinkin.buckwheat.dao.DrawDao
import com.danilkinkin.buckwheat.entities.Draw


lateinit var instanceDB: DatabaseModule

@Database(entities = [Draw::class], version = 1)
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

    abstract fun drawDao(): DrawDao
}
