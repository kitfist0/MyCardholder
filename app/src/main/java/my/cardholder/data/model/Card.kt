package my.cardholder.data.model

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
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
    }
}
