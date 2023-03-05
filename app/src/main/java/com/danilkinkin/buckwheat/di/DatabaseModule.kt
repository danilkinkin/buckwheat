package com.danilkinkin.buckwheat.di

import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import com.danilkinkin.buckwheat.data.dao.SpentDao
import com.danilkinkin.buckwheat.data.dao.StorageDao
import com.danilkinkin.buckwheat.data.entities.Spent
import com.danilkinkin.buckwheat.data.entities.Storage

@DeleteColumn.Entries(
    DeleteColumn(
        tableName = "Spent",
        columnName = "deleted"
    )
)
class AutoMigration2to3 : AutoMigrationSpec {}

@Database(
    entities = [Spent::class, Storage::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = DatabaseModule.AutoMigration::class),
        AutoMigration(from = 2, to = 3, spec = AutoMigration2to3::class),
    ],
)
@TypeConverters(RoomConverters::class)
abstract class DatabaseModule : RoomDatabase() {
    abstract fun spentDao(): SpentDao
    abstract fun storageDao(): StorageDao

    class AutoMigration : AutoMigrationSpec
}
