package my.cardholder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class Card (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val logo: String = "",
    val color: String,
    val format: String,
    val time: Long,
)
