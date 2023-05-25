package my.cardholder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import my.cardholder.data.model.LabelRef

@Dao
interface LabelRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(labelRef: LabelRef)

    @Delete
    suspend fun delete(labelRef: LabelRef)
}
