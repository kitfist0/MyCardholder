package my.cardholder.data.source

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Card
import my.cardholder.data.model.CardAndCategory
import my.cardholder.data.model.SupportedFormat

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY id DESC")
    fun getCards(): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCard(id: Long): Flow<Card?>

    @Transaction
    @Query("SELECT * FROM cards")
    fun getCardsAndCategories(): Flow<List<CardAndCategory>>

    @Transaction
    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCardAndCategory(id: Long): Flow<CardAndCategory?>

    @Query("SELECT * FROM cards WHERE name = :name AND content = :content AND format = :format LIMIT 1")
    suspend fun getCardWithSuchData(name: String, content: String, format: SupportedFormat): Card?

    @Query("SELECT * FROM cards WHERE name LIKE :name")
    suspend fun getCardsWithNamesLike(name: String): List<Card>

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteCard(id: Long)

    @Upsert
    suspend fun upsert(card: Card): Long
}
