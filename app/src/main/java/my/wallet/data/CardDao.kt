package my.wallet.data

import androidx.room.*

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE id LIKE :id LIMIT 1")
    suspend fun getCard(id: Long): Card

    @Query("SELECT * FROM cards")
    suspend fun getCards(): List<Card>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: Card): Long

    @Update
    suspend fun update(card: Card)

    @Delete
    suspend fun delete(card: Card)
}
