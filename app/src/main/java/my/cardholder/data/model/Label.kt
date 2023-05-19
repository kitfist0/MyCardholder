package my.cardholder.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "labels",
    indices = [Index(value = ["text"], unique = true)],
)
data class Label(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val text: String,
)
