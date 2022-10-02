package my.cardholder.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Card

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE id LIKE :id LIMIT 1")
    suspend fun getCard(id: Long): Card

    @Query("SELECT * FROM cards")
    fun getCards(): Flow<List<Card>>

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteCard(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: Card): Long

    @Update
    suspend fun update(card: Card)
}
