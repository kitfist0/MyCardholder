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
    version = 6,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun categoryDao(): CategoryDao
    abstract fun coffeeDao(): CoffeeDao

    companion object {
        val MIGRATIONS = arrayOf(
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE `cards` ADD COLUMN `is_pinned` INTEGER NOT NULL DEFAULT 0")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_cards_category_id` ON `cards` (`category_id`)")
                }
            },
            object : Migration(2, 3) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    val time = System.currentTimeMillis()
                    db.execSQL("ALTER TABLE `cards` ADD COLUMN `changed_at` INTEGER NOT NULL DEFAULT $time")
                }
            },
            object : Migration(3, 4) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE `cards` ADD COLUMN `logo` TEXT DEFAULT NULL")
                }
            },
            object : Migration(4, 5) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE cards ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
                    val cursor = db.query("SELECT * FROM cards ORDER BY is_pinned")
                    var hasItem = cursor.moveToFirst()
                    var position = 0
                    while (hasItem) {
                        val idIndex = cursor.getColumnIndex("id")
                        val posIndex = cursor.getColumnIndex("position")
                        if (idIndex != -1 && posIndex != -1) {
                            val id = cursor.getLong(idIndex)
                            db.execSQL("UPDATE cards SET position = '$position' WHERE id = $id")
                        }
                        hasItem = cursor.moveToNext()
                        position++
                    }
                    cursor.close()
                }
            },
            object : Migration(5, 6) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE `cards` ADD COLUMN `comment` TEXT DEFAULT NULL")
                }
            },
        )
    }
}
