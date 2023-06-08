package my.cardholder.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Coffee

@Dao
interface CoffeeDao {
    @Query("SELECT * FROM coffees ORDER BY id")
    fun getCoffees(): Flow<List<Coffee>>

    @Query("SELECT (SELECT COUNT(*) FROM coffees) == 0")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(coffee: Coffee): Long
}
