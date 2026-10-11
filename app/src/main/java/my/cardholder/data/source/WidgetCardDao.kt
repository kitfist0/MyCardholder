package my.cardholder.data.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import my.cardholder.data.model.Card
import my.cardholder.data.model.WidgetCard

@Dao
interface WidgetCardDao {

    @Query(
        "SELECT cards.* FROM cards INNER JOIN widget_cards ON cards.id = widget_cards.card_id " +
            "WHERE widget_cards.widget_id = :widgetId ORDER BY cards.position"
    )
    suspend fun getWidgetCards(widgetId: Int): List<Card>

    @Query("SELECT card_id FROM widget_cards WHERE widget_id = :widgetId")
    suspend fun getWidgetCardIds(widgetId: Int): List<Long>

    @Transaction
    suspend fun replaceWidgetCards(widgetId: Int, widgetCards: List<WidgetCard>) {
        deleteWidgets(listOf(widgetId))
        insert(widgetCards)
    }

    @Query("DELETE FROM widget_cards WHERE widget_id IN (:widgetIds)")
    suspend fun deleteWidgets(widgetIds: List<Int>)

    @Insert
    suspend fun insert(widgetCards: List<WidgetCard>)
}
