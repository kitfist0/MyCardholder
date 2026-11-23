package my.cardholder.data.model

import androidx.core.graphics.toColorInt
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.File
import java.util.UUID

typealias BarcodeFilePath = String

@Entity(
    tableName = "cards",
    indices = [
        Index(value = ["category_id"]),
        Index(value = ["sync_id"], unique = true),
    ],
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
    val position: Int,
    val logo: String? = null,
    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean,
    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null,
    val content: String,
    val color: String,
    val comment: String? = null,
    val format: SupportedFormat,
    val path: BarcodeFilePath?,
    @ColumnInfo(name = "changed_at")
    val changedAt: Long,
    /** Identifies the same card across devices during cloud sync */
    @ColumnInfo(name = "sync_id", defaultValue = "")
    val syncId: String = UUID.randomUUID().toString(),
    /**
     * Position and category have their own change times. Reordering cards or renaming a category
     * changes many cards at once, and must not overwrite edits of those cards made on another device.
     * [changedAt] is the change time of all the other synced data.
     */
    @ColumnInfo(name = "position_changed_at", defaultValue = "0")
    val positionChangedAt: Long = changedAt,
    @ColumnInfo(name = "category_changed_at", defaultValue = "0")
    val categoryChangedAt: Long = changedAt,
) {

    @Ignore
    val barcodeFile: File? = path?.let { File(it) }

    companion object {
        const val NEW_CARD_ID = 0L

        val COLORS = arrayOf(
            "#EF5350",
            "#D04141",
            "#EC407A",
            "#CD3883",
            "#C531B8",
            "#AD43D0",
            "#7E57C2",
            "#5C6BC0",
            "#3474CC",
            "#42A5F5",
            "#297CA5",
            "#26A69A",
            "#208275",
            "#66BB6A",
            "#21862A",
            "#5F7E20",
            "#7E761F",
            "#FFCA28",
            "#FF7043",
            "#8D6E63",
            "#E0E0E0",
            "#757575",
            "#424242",
            "#5D7986",
        )

        fun Card.getColorInt(): Int {
            return runCatching { color.toColorInt() }
                .getOrDefault(0)
        }
    }
}
