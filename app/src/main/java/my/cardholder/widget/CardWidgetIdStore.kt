package my.cardholder.widget

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

/** Maps home screen widget ids to the card each of them displays. */
@Singleton
class CardWidgetIdStore @Inject constructor(
    private val preferences: SharedPreferences,
) {

    private companion object {
        const val KEY_PREFIX = "widget_card_id_"
        const val NO_CARD_ID = -1L
    }

    fun saveCardId(appWidgetId: Int, cardId: Long) {
        preferences.edit { putLong(KEY_PREFIX + appWidgetId, cardId) }
    }

    fun getCardId(appWidgetId: Int): Long? {
        val cardId = preferences.getLong(KEY_PREFIX + appWidgetId, NO_CARD_ID)
        return cardId.takeIf { it != NO_CARD_ID }
    }

    fun removeCardId(appWidgetId: Int) {
        preferences.edit { remove(KEY_PREFIX + appWidgetId) }
    }
}
