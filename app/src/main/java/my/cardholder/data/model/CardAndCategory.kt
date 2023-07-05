package my.cardholder.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class CardAndCategory(
    @Embedded
    val card: Card,

    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: Category?
)
