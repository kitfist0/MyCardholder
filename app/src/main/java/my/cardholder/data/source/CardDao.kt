package my.cardholder.data.source

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Card
import my.cardholder.data.model.CardAndCategory
import my.cardholder.data.model.CardVersion
import my.cardholder.data.model.DeletedCard
import my.cardholder.data.model.SupportedFormat

@Dao
interface CardDao {

    @Transaction
    @Query("SELECT * FROM cards ORDER BY position")
    fun getCardsAndCategories(): Flow<List<CardAndCategory>>

    @Transaction
    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCardAndCategory(id: Long): Flow<CardAndCategory?>

    @Query("SELECT sync_id, changed_at FROM cards")
    fun getCardVersions(): Flow<List<CardVersion>>

    @Query("SELECT * FROM deleted_cards")
    fun getDeletedCards(): Flow<List<DeletedCard>>

    /** After the last card: positions are not renumbered when cards are deleted, so the count may be taken */
    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM cards")
    suspend fun getNextPosition(): Int

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getCard(id: Long): Card?

    @Query("SELECT * FROM cards WHERE sync_id = :syncId")
    suspend fun getCardBySyncId(syncId: String): Card?

    @Query("SELECT * FROM cards WHERE name = :name AND content = :content AND format = :format LIMIT 1")
    suspend fun getCardWithSuchData(name: String, content: String, format: SupportedFormat): Card?

    @Query("SELECT * FROM cards WHERE name = :name AND content = :content AND format = :format")
    suspend fun getCardsWithSuchData(name: String, content: String, format: SupportedFormat): List<Card>

    @Query("SELECT * FROM deleted_cards WHERE sync_id = :syncId")
    suspend fun getDeletedCard(syncId: String): DeletedCard?

    @Query("SELECT * FROM cards WHERE name LIKE :name")
    suspend fun getCardsWithNamesLike(name: String): List<Card>

    @Query("SELECT * FROM cards WHERE category_id IN (:categoryId) AND name LIKE :name")
    suspend fun getCardsWithCategoryIdAndWithNamesLike(categoryId: Long, name: String): List<Card>

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteCard(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletedCard(deletedCard: DeletedCard)

    @Query("DELETE FROM deleted_cards WHERE sync_id = :syncId")
    suspend fun deleteDeletedCard(syncId: String)

    /** Deletes the card and leaves a tombstone so that the deletion reaches other devices */
    @Transaction
    suspend fun deleteCardAndRememberDeletion(card: Card, deletedAt: Long) {
        deleteCard(card.id)
        insertDeletedCard(DeletedCard(syncId = card.syncId, deletedAt = deletedAt))
    }

    @Upsert
    suspend fun upsert(card: Card): Long

    @Upsert
    suspend fun upsert(cards: List<Card>)
}
