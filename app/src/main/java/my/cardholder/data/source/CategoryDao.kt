package my.cardholder.data.source

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Category

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    fun getCategories(): Flow<List<Category>>

    @Query("SELECT name FROM categories")
    suspend fun getCategoryNames(): List<String>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    @Query("SELECT * FROM categories WHERE name = :name")
    suspend fun getCategoryByName(name: String): Category?

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)

    @Upsert
    suspend fun upsert(category: Category)
}
