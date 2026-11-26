package my.cardholder.ui.card.list

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.createBitmap
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.roundToInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * In a single-column list, pulls every card up over the previous one, leaving only its top
 * [stridePx] visible, so the cards always lie in a stack like real ones, and leaves room above
 * the first card for the edges of the cards stacked at the top while scrolling
 * (see [CardStackScrollEffect]). Each card casts a soft shadow on the card it covers, so that
 * cards of one color don't merge. A view shadow (elevation) wouldn't do: it falls below a card,
 * where the next card covers it, and is hardly visible at the top edge.
 */
class CardStackItemDecoration(
    private val stridePx: Int,
    private val stackTopSpacePx: Int,
    private val shadowHeightPx: Float,
    private val cornerRadiusPx: Float,
    private val isDragged: (View) -> Boolean,
) : RecyclerView.ItemDecoration() {

    private companion object {
        // Of black, at the top edge of the covering card
        const val SHADOW_ALPHA = 0.12f
    }

    private val shadowPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val shadowSource = Rect()
    private val shadowDestination = RectF()

    /**
     * The shadow around the top edge of a card, rounding its top corners: [shadowHeightPx] above
     * the edge, and down to the end of the corners. Each pixel is as dark as it is close to the
     * outline of the card, and the card itself is left transparent. It is drawn once, with the
     * corners at its ends and one column of the straight edge between them, which is stretched
     * to the width of each card, as a nine-patch.
     */
    private val shadowBitmap: Bitmap by lazy {
        val capWidth = ceil(cornerRadiusPx).toInt().coerceAtLeast(1)
        val edgeTop = ceil(shadowHeightPx).toInt().coerceAtLeast(1)
        val width = 2 * capWidth + 1
        val height = edgeTop + capWidth
        val pixels = IntArray(width * height) { index ->
            val x = index % width
            val y = index / width
            // The right corner mirrors the left one
            val capX = if (x > capWidth) width - 1 - x else x
            shadowColor(distanceToCard(capX + 0.5f, y + 0.5f - edgeTop))
        }
        createBitmap(width, height).apply { setPixels(pixels, 0, width, 0, 0, width, height) }
    }

    /**
     * Distance of a point to the outline of a card whose top left corner is at 0, 0, and whose
     * top edge is straight from x = [cornerRadiusPx]. Negative inside the card.
     */
    private fun distanceToCard(x: Float, y: Float): Float {
        return if (x >= cornerRadiusPx) {
            -y
        } else {
            val cornerCenterX = cornerRadiusPx
            val cornerCenterY = cornerRadiusPx
            if (y >= cornerCenterY) -x else hypot(x - cornerCenterX, y - cornerCenterY) - cornerRadiusPx
        }
    }

    private fun shadowColor(distance: Float): Int {
        // Half a pixel of the outline is smoothed, as the card's own edge is
        val coverage = (distance + 0.5f).coerceIn(0f, 1f)
        val strength = (1f - distance / shadowHeightPx).coerceIn(0f, 1f)
        return Color.argb((SHADOW_ALPHA * strength * coverage * 255).roundToInt(), 0, 0, 0)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val isSingleColumn = (parent.layoutManager as? GridLayoutManager)?.spanCount == 1
        val position = parent.getChildAdapterPosition(view)
        when {
            !isSingleColumn -> outRect.setEmpty()
            position == 0 -> outRect.set(0, stackTopSpacePx, 0, 0)
            else -> outRect.set(0, stridePx - view.stackedHeight(parent), 0, 0)
        }
    }

    /** Height of the card with its margins, known even before the card is measured. */
    private fun View.stackedHeight(parent: RecyclerView): Int {
        val margins = layoutParams as ViewGroup.MarginLayoutParams
        // A list that measures itself lays its cards out before its own width is set, and these
        // offsets are kept until invalidated. Its layout manager already has the measured width then.
        val parentWidth = (parent.layoutManager?.width ?: parent.width)
            .takeIf { it > 0 }
            ?: parent.resources.displayMetrics.widthPixels
        val width = parentWidth - parent.paddingLeft - parent.paddingRight -
            margins.leftMargin - margins.rightMargin
        return maxOf(measuredHeight, CardItemView.realHeightForWidth(width.coerceAtLeast(0))) +
            margins.topMargin + margins.bottomMargin
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val isSingleColumn = (parent.layoutManager as? GridLayoutManager)?.spanCount == 1
        if (!isSingleColumn) {
            return
        }
        // Above all cards, so a shadow over the dragged card would show through it
        val draggedCard = (0 until parent.childCount).map(parent::getChildAt).firstOrNull(isDragged)
        for (index in 0 until parent.childCount) {
            val child = parent.getChildAt(index)
            if (parent.getChildAdapterPosition(child) <= 0 || child.alpha == 0f || child == draggedCard) {
                continue
            }
            drawShadow(canvas, child, draggedCard)
        }
    }

    /** Draws the shadow of the card on the card above it, around its top edge */
    private fun drawShadow(canvas: Canvas, card: View, draggedCard: View?) {
        // CardStackScrollEffect scales cards around the middle of their top edge
        val scaledWidth = card.width * card.scaleX
        val left = card.left + card.translationX + (card.width - scaledWidth) / 2
        val right = left + scaledWidth
        val edgeTop = card.top + card.translationY
        val top = edgeTop - shadowHeightPx * card.scaleY
        // Down to the end of the rounded corners, where the shadow goes around them
        val bottom = edgeTop + cornerRadiusPx * card.scaleY
        val capWidth = cornerRadiusPx * card.scaleX
        if (right - left <= 2 * capWidth || draggedCard != null && overlaps(draggedCard, top, bottom)) {
            return
        }
        val bitmap = shadowBitmap
        val bitmapCapWidth = bitmap.width / 2
        shadowPaint.alpha = (card.alpha * 255).toInt()
        // The rounded corners at both ends, and the straight edge stretched between them
        drawShadowPart(canvas, bitmap, 0, bitmapCapWidth, left, left + capWidth, top, bottom)
        drawShadowPart(canvas, bitmap, bitmapCapWidth, bitmapCapWidth + 1, left + capWidth, right - capWidth, top, bottom)
        drawShadowPart(canvas, bitmap, bitmapCapWidth + 1, bitmap.width, right - capWidth, right, top, bottom)
    }

    private fun drawShadowPart(
        canvas: Canvas,
        bitmap: Bitmap,
        sourceLeft: Int,
        sourceRight: Int,
        left: Float,
        right: Float,
        top: Float,
        bottom: Float,
    ) {
        shadowSource.set(sourceLeft, 0, sourceRight, bitmap.height)
        shadowDestination.set(left, top, right, bottom)
        canvas.drawBitmap(bitmap, shadowSource, shadowDestination, shadowPaint)
    }

    private fun overlaps(view: View, top: Float, bottom: Float): Boolean {
        val viewTop = view.top + view.translationY
        return viewTop < bottom && viewTop + view.height > top
    }
}
