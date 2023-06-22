package my.cardholder.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)],
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
)
