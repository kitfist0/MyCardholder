package my.cardholder.data

import androidx.room.Database
import androidx.room.RoomDatabase
import my.cardholder.data.model.Card
import my.cardholder.data.model.Coffee
import my.cardholder.data.model.Label

@Database(
    entities = [Card::class, Coffee::class, Label::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun coffeeDao(): CoffeeDao
    abstract fun labelDao(): LabelDao
}
