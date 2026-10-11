package my.cardholder.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.PendingIntentCompat
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import my.cardholder.R
import my.cardholder.data.CardRepository
import my.cardholder.shortcut.CardShortcutManager
import my.cardholder.ui.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Home screen widgets showing the cards the user has chosen for each of them.
 *
 * Cards are provided by [CardWidgetService], although collections backed by a service are
 * deprecated since API 35. RemoteCollectionItems replacing them would pass all cards with their
 * logo bitmaps to the launcher in a single transaction, which a widget with many cards exceeds.
 */
@Singleton
class CardWidgetManager @Inject constructor(
    private val context: Context,
    private val cardRepository: CardRepository,
) {

    private val appWidgetManager: AppWidgetManager
        get() = AppWidgetManager.getInstance(context)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Keeps widgets up to date with cards changed in any way: edited, reordered, deleted, synced. */
    fun observeCardChanges() {
        cardRepository.cardsAndCategories
            // The first value is what widgets already show.
            .drop(1)
            .onEach { notifyCardsChanged() }
            .launchIn(scope)
    }

    @Suppress("DEPRECATION") // See the class description.
    fun updateWidgets(widgetIds: IntArray) {
        widgetIds.forEach { widgetId ->
            appWidgetManager.updateAppWidget(widgetId, buildRemoteViews(widgetId))
        }
        // A widget that already shows cards keeps them until its data is reloaded.
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_cards_grid)
    }

    @Suppress("DEPRECATION") // See the class description.
    private fun notifyCardsChanged() {
        val widgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, CardWidgetProvider::class.java))
        if (widgetIds.isNotEmpty()) {
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widget_cards_grid)
        }
    }

    @Suppress("DEPRECATION") // See the class description.
    private fun buildRemoteViews(widgetId: Int): RemoteViews {
        val serviceIntent = Intent(context, CardWidgetService::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        // Intents differing only in extras are equal, and each widget needs its own items factory.
        serviceIntent.data = serviceIntent.toUri(Intent.URI_INTENT_SCHEME).toUri()

        return RemoteViews(context.packageName, R.layout.widget_cards).apply {
            setRemoteAdapter(R.id.widget_cards_grid, serviceIntent)
            setEmptyView(R.id.widget_cards_grid, R.id.widget_cards_empty_text)
            setPendingIntentTemplate(R.id.widget_cards_grid, openCardPendingIntentTemplate())
            setOnClickPendingIntent(R.id.widget_cards_empty_text, configurePendingIntent(widgetId))
        }
    }

    // Completed with the card id by each item, see CardWidgetItemsFactory.
    private fun openCardPendingIntentTemplate() = PendingIntentCompat.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).setAction(Intent.ACTION_VIEW),
        0,
        true,
    )

    private fun configurePendingIntent(widgetId: Int) = PendingIntentCompat.getActivity(
        context,
        widgetId,
        Intent(context, CardWidgetConfigureActivity::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId),
        0,
        false,
    )

    companion object {
        fun openCardFillInIntent(cardId: Long): Intent =
            Intent().putExtra(CardShortcutManager.EXTRA_CARD_ID, cardId)
    }
}
