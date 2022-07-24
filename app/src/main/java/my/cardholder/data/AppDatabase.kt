package my.cardholder.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Card::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
}
