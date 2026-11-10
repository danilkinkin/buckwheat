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

/** name, emoji, ARGB colour. Order matters: it is also the display order. */
private val defaultCategories: List<Triple<String, String, Long>> = listOf(
    Triple("Food & Dining", "🍜", 0xFFFF8A65),
    Triple("Groceries", "🛒", 0xFF9CCC65),
    Triple("Transport", "🚌", 0xFF4FC3F7),
    Triple("Home & Bills", "🏠", 0xFF81C784),
    Triple("Mobile & Internet", "📶", 0xFF4DD0E1),
    Triple("Streaming", "📺", 0xFFBA68C8),
    Triple("Music", "🎧", 0xFFF06292),
    Triple("Digital & AI", "☁️", 0xFF7986CB),
    Triple("Shopping", "🛍️", 0xFFFFB74D),
    Triple("Entertainment", "🎬", 0xFF9575CD),
    Triple("Health & Fitness", "🏋️", 0xFFE57373),
    Triple("Travel", "✈️", 0xFF4DB6AC),
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

private fun renameCategory(
    database: SupportSQLiteDatabase,
    oldName: String,
    oldEmoji: String,
    newName: String,
    newEmoji: String,
    color: Long,
) {
    database.execSQL(
        "UPDATE `categories` SET `name` = ?, `emoji` = ?, `color` = ? " +
                "WHERE `name` = ? AND `emoji` = ?",
        arrayOf<Any>(newName, newEmoji, color, oldName, oldEmoji),
    )
}

private fun ensureCategory(
    database: SupportSQLiteDatabase,
    name: String,
    emoji: String,
    color: Long,
) {
    database.execSQL(
        "INSERT INTO `categories` (`name`, `emoji`, `color`) " +
                "SELECT ?, ?, ? WHERE NOT EXISTS " +
                "(SELECT 1 FROM `categories` WHERE `name` = ?)",
        arrayOf<Any>(name, emoji, color, name),
    )
}

/** Retroactively categorise subscriptions that already carry a service but still sit in Other. */
private fun assignServiceCategory(
    database: SupportSQLiteDatabase,
    categoryName: String,
    serviceIds: List<String>,
) {
    if (serviceIds.isEmpty()) return

    val placeholders = serviceIds.joinToString(", ") { "?" }
    database.execSQL(
        "UPDATE `subscriptions` SET `categoryId` = " +
                "(SELECT `id` FROM `categories` WHERE `name` = ? LIMIT 1) " +
                "WHERE `serviceId` IN ($placeholders) AND " +
                "(`categoryId` IS NULL OR `categoryId` = " +
                "(SELECT `id` FROM `categories` WHERE `name` = 'Other' LIMIT 1))",
        arrayOf<Any>(categoryName, *serviceIds.toTypedArray()),
    )
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

/** Optional catalog service/bundle chosen for a subscription. */
val AutoMigration7to8: Migration = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `subscriptions` ADD COLUMN `serviceId` TEXT")
    }
}

/**
 * Custom intervals: the period column becomes the unit (DAYS/WEEKS/MONTHS/YEARS) and
 * intervalCount carries how many of them. Existing rows are all count = 1.
 */
val AutoMigration8to9: Migration = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE `subscriptions` ADD COLUMN `intervalCount` INTEGER NOT NULL DEFAULT 1"
        )
        database.execSQL(
            "UPDATE `subscriptions` SET `period` = CASE `period` " +
                    "WHEN 'WEEKLY' THEN 'WEEKS' " +
                    "WHEN 'MONTHLY' THEN 'MONTHS' " +
                    "WHEN 'YEARLY' THEN 'YEARS' " +
                    "ELSE `period` END"
        )
    }
}

/**
 * Refines the category set for subscriptions: renames the four seeded categories that are too
 * vague, adds the missing purpose categories and keeps every existing transaction attached by
 * updating rows in place instead of replacing them.
 */
val AutoMigration9to10: Migration = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        renameCategory(database, "Food", "🍜", "Food & Dining", "🍜", 0xFFFF8A65)
        renameCategory(database, "Home", "🏠", "Home & Bills", "🏠", 0xFF81C784)
        renameCategory(database, "Fun", "🎮", "Entertainment", "🎬", 0xFF9575CD)
        renameCategory(database, "Health", "💊", "Health & Fitness", "🏋️", 0xFFE57373)

        defaultCategories.forEach { (name, emoji, color) ->
            ensureCategory(database, name, emoji, color)
        }

        assignServiceCategory(
            database,
            "Mobile & Internet",
            listOf("jio", "airtel"),
        )
        assignServiceCategory(
            database,
            "Food & Dining",
            listOf("swiggy_one", "zomato_one"),
        )
        assignServiceCategory(
            database,
            "Music",
            listOf("apple_music_family", "spotify", "youtube_premium"),
        )
        assignServiceCategory(
            database,
            "Health & Fitness",
            listOf("cult_fit"),
        )
        assignServiceCategory(
            database,
            "Digital & AI",
            listOf(
                "icloud_plus", "google_one", "opencode", "chatgpt", "claude", "gemini",
                "perplexity", "github_copilot", "microsoft_365", "canva", "notion",
            ),
        )
        assignServiceCategory(
            database,
            "Streaming",
            listOf(
                "jiohotstar", "netflix", "prime_video", "apple_tv", "zee5", "sonyliv",
                "aha", "sun_nxt", "apple_one_family", "amazon_prime",
            ),
        )
    }
}

/**
 * Subscription-posted transactions remember which service paid for them, so the ledger can show
 * the service logo instead of the category emoji. Existing rows are backfilled by matching the
 * subscription name that was used as the comment.
 */
val AutoMigration10to11: Migration = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `transactions` ADD COLUMN `serviceId` TEXT")
        database.execSQL(
            "UPDATE `transactions` SET `serviceId` = (" +
                    "SELECT `s`.`serviceId` FROM `subscriptions` `s` " +
                    "WHERE `s`.`serviceId` IS NOT NULL AND `s`.`name` = `transactions`.`comment` " +
                    "LIMIT 1) " +
                    "WHERE `transactions`.`type` = 'SPENT' " +
                    "AND `transactions`.`serviceId` IS NULL " +
                    "AND EXISTS (SELECT 1 FROM `subscriptions` `s` " +
                    "WHERE `s`.`serviceId` IS NOT NULL AND `s`.`name` = `transactions`.`comment`)"
        )
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
    version = 11,
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
            AutoMigration7to8,
            AutoMigration8to9,
            AutoMigration9to10,
            AutoMigration10to11,
        )
    }
}
