package my.cardholder.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CardWithLabels(
    @Embedded
    val card: Card,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            LabelRef::class,
            parentColumn = "card_id",
            entityColumn = "label_id"
        ),
    )
    val labels: List<Label>
)
