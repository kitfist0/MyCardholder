package my.cardholder.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CardWithLabels(
    @Embedded val card: Card,
    @Relation(
        parentColumn = "id",
        entityColumn = "cardId",
        associateBy = Junction(LabelRef::class),
    )
    val labels: List<Label>
)
