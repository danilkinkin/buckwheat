package com.danilkinkin.buckwheat.di

import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.danilkinkin.buckwheat.data.dao.CategoryDao
import com.danilkinkin.buckwheat.data.dao.SubscriptionDao
import com.danilkinkin.buckwheat.data.dao.TransactionDao
import com.danilkinkin.buckwheat.data.entities.Category
import com.danilkinkin.buckwheat.data.entities.Subscription
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

/** name, emoji, ARGB colour. */
private val defaultCategories: List<Triple<String, String, Long>> = listOf(
    Triple("Food", "🍜", 0xFFFF8A65),
    Triple("Transport", "🚌", 0xFF4FC3F7),
    Triple("Home", "🏠", 0xFF81C784),
    Triple("Fun", "🎮", 0xFFBA68C8),
    Triple("Health", "💊", 0xFFE57373),
    Triple("Other", "📦", 0xFF90A4AE),
)

private fun seedDefaultCategories(database: SupportSQLiteDatabase) {
    defaultCategories.forEach { (name, emoji, color) ->
        database.execSQL(
            "INSERT INTO `categories` (`name`, `emoji`, `color`) VALUES (?, ?, ?)",
            arrayOf<Any>(name, emoji, color),
        )
    }
}

/**
 * v5 -> v6: categories, the ledger's categoryId and the subscriptions table.
 *
 * All three land together while the database is still small, so subscriptions never need a
 * second migration. Existing expenses keep `categoryId = NULL` (Uncategorised).
 */
val AutoMigration5to6: Migration = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `categories` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`emoji` TEXT NOT NULL, " +
                    "`color` INTEGER NOT NULL)"
        )

        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `subscriptions` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`amount` TEXT NOT NULL, " +
                    "`period` TEXT NOT NULL, " +
                    "`anchorDay` INTEGER NOT NULL, " +
                    "`nextDue` INTEGER NOT NULL, " +
                    "`categoryId` INTEGER, " +
                    "`active` INTEGER NOT NULL)"
        )

        database.execSQL("ALTER TABLE `transactions` ADD COLUMN `categoryId` INTEGER")
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_transactions_categoryId` " +
                    "ON `transactions` (`categoryId`)"
        )

        seedDefaultCategories(database)
    }
}

/**
 * Drops the legacy `storage` key/value table. It was only read by the one-time migration to
 * DataStore, which is gone; the table is empty on every install that started after the move.
 */
val AutoMigration6to7: Migration = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS `storage`")
    }
}

/** Fresh installs skip migrations, so the same seed runs after Room creates the tables. */
val SeedDefaultCategoriesCallback = object : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        seedDefaultCategories(db)
    }
}

@Database(
    entities = [Transaction::class, Category::class, Subscription::class],
    version = 7,
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

    abstract fun categoryDao(): CategoryDao

    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        val MANUAL_MIGRATIONS = arrayOf<Migration>(
            AutoMigration4to5,
            AutoMigration5to6,
            AutoMigration6to7,
        )
    }
}
