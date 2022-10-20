package my.cardholder.data.model

import android.content.Context
import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.material.color.MaterialColors
import com.google.android.material.R
import java.io.File

@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val text: String,
    val color: String,
    val timestamp: Long,
    val format: SupportedFormat,
) {
    companion object {
        fun Card.getBarcodeFile(context: Context): File {
            return File(context.getExternalFilesDir("images"), "$timestamp.jpeg")
        }

        fun Card.getColorInt(context: Context): Int {
            return try {
                Color.parseColor(color)
            } catch (e: Exception) {
                MaterialColors.getColor(context, R.attr.colorSecondary, Color.WHITE)
            }
        }
    }
}
