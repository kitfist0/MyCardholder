package my.cardholder.data.model

import androidx.core.graphics.toColorInt
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val text: String,
    val color: String = COLORS.random(),
    val timestamp: Long,
    val format: SupportedFormat,
) {
    @Ignore
    val barcodeFileName = "$timestamp.jpeg"

    companion object {
        val COLORS = arrayOf(
            "#EF5350",
            "#7E57C2",
            "#5C6BC0",
            "#26A69A",
            "#66BB6A",
            "#FFCA28",
            "#FF7043",
            "#8D6E63",
        )

        fun Card.getColorInt() = try {
            color.toColorInt()
        } catch (e: IllegalArgumentException) {
            0
        }
    }
}
