package my.cardholder.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.RemoteViewsService
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.data.CardWidgetRepository
import javax.inject.Inject

@AndroidEntryPoint
class CardWidgetService : RemoteViewsService() {

    @Inject
    lateinit var cardWidgetRepository: CardWidgetRepository

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return CardWidgetItemsFactory(
            context = applicationContext,
            widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID),
            cardWidgetRepository = cardWidgetRepository,
        )
    }
}
