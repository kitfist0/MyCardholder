package my.cardholder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Label

@Dao
interface LabelDao {
    @Query("SELECT * FROM labels")
    fun getLabels(): Flow<List<Label>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(label: Label)
}
