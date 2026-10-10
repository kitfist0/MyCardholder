package my.cardholder.widget

import android.content.Context
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.graphics.ColorUtils
import kotlinx.coroutines.runBlocking
import my.cardholder.R
import my.cardholder.data.CardWidgetRepository
import my.cardholder.data.model.Card
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.data.model.isSquare
import my.cardholder.util.LogoLoader

/**
 * Cards of one widget. The system calls it on a binder thread, where blocking for the database
 * and logos is allowed.
 */
class CardWidgetItemsFactory(
    private val context: Context,
    private val widgetId: Int,
    private val cardWidgetRepository: CardWidgetRepository,
) : RemoteViewsService.RemoteViewsFactory {

    private companion object {
        const val LIGHT_COLOR_LUMINANCE = 0.5

        // The launcher shows the widget once all of its cards are loaded, so a logo that can't be
        // loaded quickly is replaced with the barcode icon until the next update.
        const val LOGO_LOAD_TIMEOUT_MS = 1000L
    }

    private var cards = emptyList<Card>()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        cards = runBlocking { cardWidgetRepository.getWidgetCards(widgetId) }
    }

    override fun onDestroy() {
        cards = emptyList()
    }

    override fun getCount(): Int = cards.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.item_widget_card)
        val card = cards.getOrNull(position) ?: return views
        val colorInt = card.getColorInt()
        views.setInt(R.id.item_widget_card_background_image, "setColorFilter", colorInt)
        views.setTextViewText(R.id.item_widget_card_name_text, card.name)
        views.setTextColor(
            R.id.item_widget_card_name_text,
            if (ColorUtils.calculateLuminance(colorInt) > LIGHT_COLOR_LUMINANCE) Color.BLACK else Color.WHITE
        )
        val logoBitmap = card.logo?.ifEmpty { null }?.let { logoUrl ->
            val resources = context.resources
            runBlocking {
                LogoLoader(context).loadBitmap(
                    imageUrl = logoUrl,
                    sizePx = resources.getDimensionPixelSize(R.dimen.widget_card_logo_size) -
                        2 * resources.getDimensionPixelSize(R.dimen.widget_card_logo_padding),
                    paddingPx = 0,
                    cornerRadiusPx = 0,
                    timeoutMs = LOGO_LOAD_TIMEOUT_MS,
                )
            }
        }
        if (logoBitmap != null) {
            views.setImageViewBitmap(R.id.item_widget_card_logo_image, logoBitmap)
        } else {
            views.setImageViewResource(
                R.id.item_widget_card_logo_image,
                if (card.format.isSquare()) R.drawable.ic_qr_code else R.drawable.ic_barcode
            )
        }
        views.setOnClickFillInIntent(R.id.item_widget_card_root, CardWidgetManager.openCardFillInIntent(card.id))
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = cards.getOrNull(position)?.id ?: position.toLong()

    override fun hasStableIds(): Boolean = true
}
