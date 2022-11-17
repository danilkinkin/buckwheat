package com.danilkinkin.buckwheat.di

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.danilkinkin.buckwheat.data.dao.SpentDao
import com.danilkinkin.buckwheat.data.dao.StorageDao
import com.danilkinkin.buckwheat.data.entities.Spent
import com.danilkinkin.buckwheat.data.entities.Storage

@Database(
    entities = [Spent::class, Storage::class],
    version = 2,
    autoMigrations = [
        AutoMigration (from = 1, to = 2, spec = DatabaseModule.AutoMigration::class)
    ],
)
@TypeConverters(RoomConverters::class)
abstract class DatabaseModule : RoomDatabase() {
    abstract fun spentDao(): SpentDao
    abstract fun storageDao(): StorageDao

    class AutoMigration : AutoMigrationSpec
}
