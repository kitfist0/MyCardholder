package my.cardholder.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** A card the user has chosen to show in the home screen widget with [widgetId]. */
@Entity(
    tableName = "widget_cards",
    primaryKeys = ["widget_id", "card_id"],
    indices = [Index(value = ["card_id"])],
    foreignKeys = [
        ForeignKey(
            entity = Card::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("card_id"),
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class WidgetCard(
    @ColumnInfo(name = "widget_id")
    val widgetId: Int,
    @ColumnInfo(name = "card_id")
    val cardId: Long,
)
