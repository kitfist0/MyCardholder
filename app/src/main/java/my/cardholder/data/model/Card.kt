package my.cardholder.data.model

import android.content.Context
import android.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File

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

        fun Card.getBarcodeFile(context: Context): File {
            return File(context.getExternalFilesDir("images"), "$timestamp.jpeg")
        }

        fun Card.getColorInt(): Int {
            return try {
                color.toColorInt()
            } catch (e: IllegalArgumentException) {
                Color.MAGENTA
            }
        }
    }
}
