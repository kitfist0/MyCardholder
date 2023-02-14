package my.cardholder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coffees")
data class Coffee(
    @PrimaryKey
    val id: String,
    val name: String,
    val isPurchased: Boolean,
)
