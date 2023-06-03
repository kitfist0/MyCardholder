package my.cardholder.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "label_refs",
    primaryKeys = ["card_id", "label_id"],
    indices = [Index("card_id"), Index("label_id")],
    foreignKeys = [
        ForeignKey(
            entity = Card::class,
            parentColumns = ["id"],
            childColumns = ["card_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Label::class,
            parentColumns = ["id"],
            childColumns = ["label_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ]
)
data class LabelRef(
    @ColumnInfo("card_id")
    val cardId: Long,
    @ColumnInfo("label_id")
    val labelId: Long,
)
