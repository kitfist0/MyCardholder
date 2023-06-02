package my.cardholder.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Label

@Dao
interface LabelDao {
    @Query("SELECT * FROM labels")
    fun getLabels(): Flow<List<Label>>

    @Query("SELECT * FROM labels WHERE id = :id")
    suspend fun getLabel(id: Long): Label?

    @Query("SELECT * FROM labels WHERE text = :text")
    suspend fun getLabelByText(text: String): Label?

    @Query("DELETE FROM labels WHERE id = :id")
    suspend fun deleteLabel(id: Long)

    @Upsert
    suspend fun upsert(label: Label)

    @Upsert
    suspend fun upsert(labels: List<Label>): List<Long>
}
