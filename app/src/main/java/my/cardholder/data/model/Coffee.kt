package my.cardholder.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "coffees")
data class Coffee(
    @PrimaryKey
    val id: String,
    val isPurchased: Boolean,
) {
    @Ignore
    val name: String = id.split(".")[1].replaceFirstChar(Char::titlecase)
}
