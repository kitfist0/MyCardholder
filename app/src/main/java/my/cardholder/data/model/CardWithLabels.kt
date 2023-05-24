package my.cardholder.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CardWithLabels(
    @Embedded
    val card: Card,

    @Relation(
        entity = Label::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            LabelRef::class,
            parentColumn = "cardId",
            entityColumn = "labelId"
        ),
    )
    val labels: List<Label>
)
