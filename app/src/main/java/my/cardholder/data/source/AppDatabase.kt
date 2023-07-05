package my.cardholder.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import my.cardholder.data.model.Card
import my.cardholder.data.model.Category
import my.cardholder.data.model.Coffee

@Database(
    entities = [
        Card::class,
        Category::class,
        Coffee::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun categoryDao(): CategoryDao
    abstract fun coffeeDao(): CoffeeDao
}
