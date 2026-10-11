package my.cardholder.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import my.cardholder.data.CardWidgetRepository
import javax.inject.Inject

@AndroidEntryPoint
class CardWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var cardWidgetManager: CardWidgetManager

    @Inject
    lateinit var cardWidgetRepository: CardWidgetRepository

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        cardWidgetManager.updateWidgets(appWidgetIds)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cardWidgetRepository.deleteWidgets(appWidgetIds.toList())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
