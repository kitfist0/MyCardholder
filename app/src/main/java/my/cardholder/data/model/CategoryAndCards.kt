package my.cardholder.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryAndCards(
    @Embedded
    val category: Category,

    @Relation(
        parentColumn = "id",
        entityColumn = "category_id"
    )
    val cards: List<Card>
)
