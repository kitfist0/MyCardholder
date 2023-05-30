package my.cardholder.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Card
import my.cardholder.data.model.CardWithLabels

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY id DESC")
    fun getCards(): Flow<List<Card>>

    @Transaction
    @Query("SELECT * FROM cards")
    fun getCardsWithLabels(): Flow<List<CardWithLabels>>

    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCard(id: Long): Flow<Card?>

    @Transaction
    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCardWithLabels(id: Long): Flow<CardWithLabels?>

    @Query("SELECT * FROM cards WHERE name LIKE :name")
    suspend fun getCardsWithNamesLike(name: String): List<Card>

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteCard(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: Card): Long

    @Update
    suspend fun update(card: Card)
}
