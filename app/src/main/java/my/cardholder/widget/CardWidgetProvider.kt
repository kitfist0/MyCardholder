package my.cardholder.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CardWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var cardWidgetUpdater: CardWidgetUpdater

    @Inject
    lateinit var cardWidgetIdStore: CardWidgetIdStore

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            cardWidgetUpdater.updateWidgets(appWidgetIds.toList())
            pendingResult.finish()
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { cardWidgetIdStore.removeCardId(it) }
    }
}
