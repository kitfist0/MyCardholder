package my.cardholder.data.source

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Coffee

@Dao
interface CoffeeDao {
    @Query("SELECT * FROM coffees ORDER BY id")
    fun getCoffees(): Flow<List<Coffee>>

    @Query("SELECT (SELECT COUNT(*) FROM coffees) == 0")
    suspend fun isEmpty(): Boolean

    @Upsert
    suspend fun upsert(coffees: List<Coffee>)
}
