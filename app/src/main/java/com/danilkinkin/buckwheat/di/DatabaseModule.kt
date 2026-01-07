package com.danilkinkin.buckwheat.di

import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.danilkinkin.buckwheat.data.dao.ArchivedTransactionDao
import com.danilkinkin.buckwheat.data.dao.PeriodDao
import com.danilkinkin.buckwheat.data.dao.StorageDao
import com.danilkinkin.buckwheat.data.dao.TransactionDao
import com.danilkinkin.buckwheat.data.entities.ArchivedTransaction
import com.danilkinkin.buckwheat.data.entities.Period
import com.danilkinkin.buckwheat.data.entities.Storage
import com.danilkinkin.buckwheat.data.entities.Transaction


class AutoMigration1to2 : AutoMigrationSpec

@DeleteColumn.Entries(
    DeleteColumn(
        tableName = "Spent",
        columnName = "deleted"
    )
)
class AutoMigration2to3 : AutoMigrationSpec

// Preparing for remove storage table
class AutoMigration3to4 : AutoMigrationSpec

// Rename Spent to Transaction
val AutoMigration4to5: Migration = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create the new "transactions" table
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `transactions` " +
                    "(`type` TEXT NOT NULL, " +
                    "`value` TEXT NOT NULL, " +
                    "`date` INTEGER NOT NULL, " +
                    "`comment` TEXT NOT NULL DEFAULT '', " +
                    "`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)"
        )

        // Copy data from the old "Spent" table to the new "transactions" table
        database.execSQL(
            "INSERT INTO `transactions` (`type`, `value`, `date`, `comment`) " +
                    "SELECT 'SPENT', `value`, `date`, `comment` FROM `Spent`"
        )

        // Drop the old "Spent" table
        database.execSQL("DROP TABLE IF EXISTS `Spent`")
    }
}

// Add Period and ArchivedTransaction tables for history feature
val AutoMigration5to6: Migration = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create the "periods" table
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `periods` " +
                    "(`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`budget` TEXT NOT NULL, " +
                    "`spent` TEXT NOT NULL, " +
                    "`currency` TEXT NOT NULL, " +
                    "`start_date` INTEGER NOT NULL, " +
                    "`finish_date` INTEGER NOT NULL, " +
                    "`actual_finish_date` INTEGER NOT NULL, " +
                    "`archived_at` INTEGER NOT NULL)"
        )

        // Create the "archived_transactions" table
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `archived_transactions` " +
                    "(`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`period_id` INTEGER NOT NULL, " +
                    "`type` TEXT NOT NULL, " +
                    "`value` TEXT NOT NULL, " +
                    "`date` INTEGER NOT NULL, " +
                    "`comment` TEXT NOT NULL DEFAULT '', " +
                    "FOREIGN KEY(`period_id`) REFERENCES `periods`(`uid`) ON DELETE CASCADE)"
        )

        // Create index on period_id for archived_transactions
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_archived_transactions_period_id` " +
                    "ON `archived_transactions` (`period_id`)"
        )
    }
}

@Database(
    entities = [Transaction::class, Storage::class, Period::class, ArchivedTransaction::class],
    version = 6,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = AutoMigration1to2::class),
        AutoMigration(from = 2, to = 3, spec = AutoMigration2to3::class),
        AutoMigration(from = 3, to = 4, spec = AutoMigration3to4::class),
    ],
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class DatabaseModule : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun storageDao(): StorageDao

    abstract fun periodDao(): PeriodDao

    abstract fun archivedTransactionDao(): ArchivedTransactionDao

    companion object {
        val MANUAL_MIGRATIONS = arrayOf<Migration>(AutoMigration4to5, AutoMigration5to6)
    }
}
