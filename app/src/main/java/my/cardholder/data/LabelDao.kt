package my.cardholder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Label

@Dao
interface LabelDao {
    @Query("SELECT * FROM labels")
    fun getLabels(): Flow<List<Label>>

    @Query("SELECT * FROM labels WHERE id = :id")
    suspend fun getLabel(id: Long): Label?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(label: Label)

    @Delete
    suspend fun delete(label: Label)
}
