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
    val id: Long = NEW_CATEGORY_ID,
    val name: String,
) {
    companion object {
        const val MAX_NAME_LENGTH = 30
        const val NEW_CATEGORY_ID = 0L
        const val NULL_NAME = "NULL" // This name is forbidden
    }
}
