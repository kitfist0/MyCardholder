package my.cardholder.shortcut

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.IconCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import my.cardholder.R
import my.cardholder.data.CardRepository
import my.cardholder.data.model.Card
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.data.model.isSquare
import my.cardholder.ui.MainActivity
import my.cardholder.util.LogoLoader
import javax.inject.Inject
import javax.inject.Singleton

/** Home screen shortcuts: one per pinned card, plus the app shortcut that opens the scanner. */
@Singleton
class CardShortcutManager @Inject constructor(
    private val context: Context,
    private val cardRepository: CardRepository,
) {

    companion object {
        const val EXTRA_CARD_ID = "extra_shortcut_card_id"
        const val ACTION_SCAN_CARD = "my.cardholder.action.SCAN_CARD"

        private const val SHORTCUT_ID_PREFIX = "card_"
        private const val SCAN_SHORTCUT_ID = "scan"

        // Adaptive icon canvas is 108dp; launchers may mask anything outside the central 66dp circle,
        // so the logo is kept small enough to fit inside that circle.
        private const val ICON_SIZE_DP = 108
        private const val LOGO_SIZE_DP = 44

        private const val LOGO_PADDING_DP = 4
        private const val LOGO_CORNER_RADIUS_DP = 8
    }

    /** Adds (or refreshes the label of) the scanner shortcut in the app icon's long-press menu. */
    fun publishScanShortcut() {
        val intent = Intent(context, MainActivity::class.java).setAction(ACTION_SCAN_CARD)
        val shortcut = ShortcutInfoCompat.Builder(context, SCAN_SHORTCUT_ID)
            .setShortLabel(context.getString(R.string.card_scan_label))
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_menu_scan))
            .setIntent(intent)
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    /** Result for a CREATE_SHORTCUT request: the launcher pins the shortcut it describes. */
    suspend fun createShortcutResultIntent(card: Card): Intent {
        return ShortcutManagerCompat.createShortcutResultIntent(context, buildCardShortcut(card))
    }

    /** Asks the launcher to pin the card's shortcut; returns false if the launcher doesn't support pinning. */
    suspend fun requestPinShortcut(card: Card): Boolean {
        return ShortcutManagerCompat.isRequestPinShortcutSupported(context) && ShortcutManagerCompat.requestPinShortcut(context, buildCardShortcut(card), null)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Disables shortcuts of cards deleted in any way: by the user or by cloud sync. */
    fun observeCardDeletions() {
        cardRepository.cardsAndCategories
            .map { cardsAndCategories -> cardsAndCategories.mapTo(mutableSetOf()) { it.card.id } }
            .distinctUntilChanged()
            .onEach { cardIds -> disableShortcutsOfMissingCards(cardIds) }
            .launchIn(scope)
    }

    /** Pinned shortcuts can't be removed by the app, so a deleted card's shortcut is greyed out instead. */
    private fun disableShortcutsOfMissingCards(cardIds: Set<Long>) {
        val shortcutIds = ShortcutManagerCompat.getShortcuts(context, ShortcutManagerCompat.FLAG_MATCH_PINNED)
            .filter { shortcut -> shortcut.isEnabled && shortcut.id.toCardId()?.let { it !in cardIds } == true }
            .map { shortcut -> shortcut.id }
        if (shortcutIds.isNotEmpty()) {
            ShortcutManagerCompat.disableShortcuts(
                context,
                shortcutIds,
                context.getString(R.string.shortcut_card_removed_message),
            )
        }
    }

    private fun shortcutId(cardId: Long) = SHORTCUT_ID_PREFIX + cardId

    private fun String.toCardId(): Long? {
        return if (startsWith(SHORTCUT_ID_PREFIX)) removePrefix(SHORTCUT_ID_PREFIX).toLongOrNull() else null
    }

    private suspend fun buildCardShortcut(card: Card): ShortcutInfoCompat {
        val intent = Intent(context, MainActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .putExtra(EXTRA_CARD_ID, card.id)
        return ShortcutInfoCompat.Builder(context, shortcutId(card.id))
            .setShortLabel(card.name.ifBlank { context.getString(R.string.app_name) })
            .setIcon(IconCompat.createWithAdaptiveBitmap(buildIconBitmap(card)))
            .setIntent(intent)
            .build()
    }

    // Full-bleed card color with the card's logo (or a barcode icon) in the middle.
    private suspend fun buildIconBitmap(card: Card): Bitmap {
        val iconSizePx = dpToPx(ICON_SIZE_DP)
        val logoSizePx = dpToPx(LOGO_SIZE_DP)

        val logoBitmap = card.logo?.ifEmpty { null }
            ?.let {
                LogoLoader(context).loadBitmap(
                    imageUrl = it,
                    sizePx = logoSizePx,
                    paddingPx = dpToPx(LOGO_PADDING_DP),
                    cornerRadiusPx = dpToPx(LOGO_CORNER_RADIUS_DP),
                )
            }
            ?: whiteBackgroundIconBitmap(
                iconRes = if (card.format.isSquare()) R.drawable.ic_qr_code else R.drawable.ic_barcode,
                sizePx = logoSizePx,
            )

        val output = createBitmap(iconSizePx, iconSizePx)
        val canvas = Canvas(output)
        canvas.drawColor(card.getColorInt())
        val offset = (iconSizePx - logoSizePx) / 2f
        canvas.drawBitmap(logoBitmap, offset, offset, null)
        return output
    }

    // The white rounded background matches what PaddedRoundedTransformation draws behind logo images.
    private fun whiteBackgroundIconBitmap(@DrawableRes iconRes: Int, sizePx: Int): Bitmap {
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

    private fun dpToPx(sizeDp: Int): Int = (sizeDp * context.resources.displayMetrics.density).toInt()
}
