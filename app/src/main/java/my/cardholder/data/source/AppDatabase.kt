package my.cardholder.data.source

import androidx.room.Database
import androidx.room.RoomDatabase
import my.cardholder.data.model.Card
import my.cardholder.data.model.Category
import my.cardholder.data.model.LabelRef
import my.cardholder.data.model.Coffee
import my.cardholder.data.model.Label

@Database(
    entities = [
        Card::class,
        Category::class,
        Coffee::class,
        Label::class,
        LabelRef::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun categoryDao(): CategoryDao
    abstract fun coffeeDao(): CoffeeDao
    abstract fun labelDao(): LabelDao
    abstract fun labelRefDao(): LabelRefDao
}
