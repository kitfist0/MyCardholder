package my.cardholder.data

import my.cardholder.data.model.Card
import my.cardholder.data.model.WidgetCard
import my.cardholder.data.source.WidgetCardDao
import javax.inject.Inject
import javax.inject.Singleton

/** Which cards each home screen widget shows. */
@Singleton
class CardWidgetRepository @Inject constructor(
    private val widgetCardDao: WidgetCardDao,
) {

    /** Cards of the widget, in the order of the card list. Deleted cards are left out. */
    suspend fun getWidgetCards(widgetId: Int): List<Card> {
        return widgetCardDao.getWidgetCards(widgetId)
    }

    suspend fun getWidgetCardIds(widgetId: Int): Set<Long> {
        return widgetCardDao.getWidgetCardIds(widgetId).toSet()
    }

    suspend fun setWidgetCards(widgetId: Int, cardIds: Set<Long>) {
        widgetCardDao.replaceWidgetCards(widgetId, cardIds.map { WidgetCard(widgetId, it) })
    }

    suspend fun deleteWidgets(widgetIds: List<Int>) {
        widgetCardDao.deleteWidgets(widgetIds)
    }
}
