package my.cardholder.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import my.cardholder.data.model.Card
import my.cardholder.data.model.Category
import my.cardholder.data.model.Coffee

@Database(
    entities = [
        Card::class,
        Category::class,
        Coffee::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun categoryDao(): CategoryDao
    abstract fun coffeeDao(): CoffeeDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `cards` ADD COLUMN `is_pinned` INTEGER DEFAULT 0 NOT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cards_category_id` ON `cards` (`category_id`)")
            }
        }
    }
}
