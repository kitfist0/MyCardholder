package my.cardholder.data.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "label_refs",
    primaryKeys = ["cardId", "labelId"],
    foreignKeys = [
        ForeignKey(
            entity = Card::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Label::class,
            parentColumns = ["id"],
            childColumns = ["labelId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ]
)
data class LabelRef(
    val cardId: Long,
    val labelId: Long,
)
