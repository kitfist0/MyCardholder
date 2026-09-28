package my.cardholder.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import my.cardholder.R
import my.cardholder.data.CardRepository
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.data.model.isSquare
import my.cardholder.ui.MainActivity
import my.cardholder.util.LogoLoader
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class CardWidgetUpdater @Inject constructor(
    private val context: Context,
    private val widgetIdStore: CardWidgetIdStore,
    private val cardRepository: CardRepository,
) {

    companion object {
        const val EXTRA_CARD_ID = "extra_widget_card_id"

        // Same values my.cardholder.util.ext.loadLogoImage uses for the card list's logo image.
        private const val LOGO_SIZE_DP = 128
        private const val LOGO_PADDING_DP = 8
        private const val LOGO_CORNER_RADIUS_DP = 16
        private const val LOGO_LOAD_TIMEOUT_MS = 4000L

        private const val CARD_BITMAP_SIZE_PX = 200
        // Matches item_search_result_card's app:cardCornerRadius="6dp" on a 55dp-wide card.
        private const val CARD_CORNER_RADIUS_RATIO = 0.11f
    }

    suspend fun updateWidgets(appWidgetIds: List<Int>) {
        if (appWidgetIds.isEmpty()) return
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetIds.forEach { appWidgetId ->
            val cardId = widgetIdStore.getCardId(appWidgetId)
            appWidgetManager.updateAppWidget(appWidgetId, buildRemoteViews(cardId))
        }
    }

    private suspend fun buildRemoteViews(cardId: Long?): RemoteViews {
        val card = cardId?.let { cardRepository.getCard(it) }
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_card)
        remoteViews.setOnClickPendingIntent(R.id.widget_card_root, createOpenAppPendingIntent(cardId))

        if (card == null) {
            remoteViews.setViewVisibility(R.id.widget_card_background_image, View.GONE)
            remoteViews.setViewVisibility(R.id.widget_card_content_layout, View.GONE)
            remoteViews.setViewVisibility(R.id.widget_card_empty_text, View.VISIBLE)
            return remoteViews
        }

        remoteViews.setViewVisibility(R.id.widget_card_background_image, View.VISIBLE)
        remoteViews.setViewVisibility(R.id.widget_card_content_layout, View.VISIBLE)
        remoteViews.setViewVisibility(R.id.widget_card_empty_text, View.GONE)

        val cardColor = card.getColorInt()
        remoteViews.setImageViewBitmap(R.id.widget_card_background_image, roundedCardBitmap(cardColor))
        remoteViews.setTextViewText(R.id.widget_card_name_text, card.name)
        remoteViews.setTextColor(R.id.widget_card_name_text, pickReadableTextColor(cardColor))

        val logoBitmap = card.logo?.ifEmpty { null }?.let { loadLogoBitmap(it) }
        val iconRes = if (card.format.isSquare()) R.drawable.ic_qr_code else R.drawable.ic_barcode
        remoteViews.setImageViewBitmap(
            R.id.widget_card_barcode_image,
            logoBitmap ?: whiteBackgroundIconBitmap(iconRes),
        )

        return remoteViews
    }

    // RemoteViews.setBackgroundColor always paints a plain rectangle, so a rounded "card" look
    // (matching item_card_widget_select's color swatch) is drawn into a bitmap instead.
    private fun roundedCardBitmap(color: Int): Bitmap {
        val bitmap = createBitmap(CARD_BITMAP_SIZE_PX, CARD_BITMAP_SIZE_PX)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        val cornerRadius = CARD_BITMAP_SIZE_PX * CARD_CORNER_RADIUS_RATIO
        Canvas(bitmap).drawRoundRect(
            RectF(0f, 0f, CARD_BITMAP_SIZE_PX.toFloat(), CARD_BITMAP_SIZE_PX.toFloat()),
            cornerRadius,
            cornerRadius,
            paint,
        )
        return bitmap
    }

    // RemoteViews.setImageViewResource is unreliable for vector drawables on some launchers, so
    // every image (logo or fallback icon) is always sent as a plain Bitmap instead. The white
    // rounded background matches what PaddedRoundedTransformation draws behind logo images.
    private fun whiteBackgroundIconBitmap(@DrawableRes iconRes: Int): Bitmap {
        val sizePx = dpToPx(LOGO_SIZE_DP)
        val paddingPx = dpToPx(LOGO_PADDING_DP)
        val cornerRadiusPx = dpToPx(LOGO_CORNER_RADIUS_DP).toFloat()

        val output = createBitmap(sizePx, sizePx)
        val canvas = Canvas(output)
        val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat()), cornerRadiusPx, cornerRadiusPx, backgroundPaint)

        val iconDrawable = ContextCompat.getDrawable(context, iconRes)!!
        iconDrawable.setBounds(paddingPx, paddingPx, sizePx - paddingPx, sizePx - paddingPx)
        iconDrawable.draw(canvas)

        return output
    }

    /** Same sizing and fallback chain as [my.cardholder.util.ext.loadLogoImage] uses in the card list. */
    private suspend fun loadLogoBitmap(logoUrl: String): Bitmap? = withTimeoutOrNull(
        LOGO_LOAD_TIMEOUT_MS.milliseconds
    ) {
        suspendCancellableCoroutine { continuation ->
            val sizePx = dpToPx(LOGO_SIZE_DP)
            LogoLoader(context).load(
                imageUrl = logoUrl,
                sizePx = sizePx,
                paddingPx = dpToPx(LOGO_PADDING_DP),
                cornerRadiusPx = dpToPx(LOGO_CORNER_RADIUS_DP),
                onSuccess = { drawable ->
                    if (continuation.isActive) {
                        continuation.resume(drawable.toBitmap(sizePx, sizePx))
                    }
                },
                onError = {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                },
            )
        }
    }

    private fun dpToPx(sizeDp: Int): Int = (sizeDp * context.resources.displayMetrics.density).toInt()

    /** White text on dark card colors, black text on light ones, for readable contrast. */
    private fun pickReadableTextColor(backgroundColor: Int): Int {
        return if (ColorUtils.calculateLuminance(backgroundColor) < 0.5) Color.WHITE else Color.BLACK
    }

    private fun createOpenAppPendingIntent(cardId: Long?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            cardId?.let { putExtra(EXTRA_CARD_ID, it) }
        }
        return PendingIntent.getActivity(
            context,
            cardId?.toInt() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
