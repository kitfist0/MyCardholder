package my.cardholder.data.model

import androidx.room.Embedded
import androidx.room.Relation
import my.cardholder.cloud.FileNameAndContent

data class CardAndCategory(
    @Embedded
    val card: Card,

    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: Category?
) {
    companion object {
        fun CardAndCategory.toFileNameAndContent(): FileNameAndContent {
            val row = arrayOf(
                "\"${card.name}\"",
                "\"${category?.name.orEmpty()}\"",
                "\"${card.content}\"",
                "\"${card.color}\"",
                "\"${card.format}\"",
            )
            return card.id.toString() to row.joinToString(separator = ",")
        }
    }
}
