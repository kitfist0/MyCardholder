package my.cardholder.data.model

import androidx.core.graphics.toColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.File

typealias BarcodeFilePath = String

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("category_id"),
            onDelete = ForeignKey.SET_NULL,
        )
    ]
)
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    @ColumnInfo("category_id")
    val categoryId: Long? = null,
    val content: String,
    val color: String,
    val format: SupportedFormat,
    val path: BarcodeFilePath?,
) {

    @Ignore
    val barcodeFile: File? = path?.let { File(it) }

    companion object {
        const val NEW_CARD_ID = 0L

        val COLORS = arrayOf(
            "#EF5350",
            "#EC407A",
            "#7E57C2",
            "#5C6BC0",
            "#42A5F5",
            "#26A69A",
            "#66BB6A",
            "#FFCA28",
            "#FF7043",
            "#8D6E63",
            "#424242",
            "#E0E0E0",
        )

        fun Card.getColorInt(): Int {
            return runCatching { color.toColorInt() }
                .getOrDefault(0)
        }
    }
}
