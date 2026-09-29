package my.cardholder.ui.card.scan

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import my.cardholder.util.ScanFrameCalculator

/**
 * Draws a dimmed scrim over the camera preview with a square cutout in the center, guiding the
 * user to place a barcode or QR code within [frameBounds]. This is the same area barcode
 * detection is restricted to, see [Barcode.isWithinScanFrame][my.cardholder.util.ext.isWithinScanFrame].
 */
class ScanFrameOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private companion object {
        const val FRAME_CORNER_RADIUS_DP = 16f
        const val FRAME_STROKE_WIDTH_DP = 2f
        const val SCRIM_COLOR = 0xAA000000.toInt()
    }

    private val density = resources.displayMetrics.density

    private val scrimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = SCRIM_COLOR
    }

    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = FRAME_STROKE_WIDTH_DP * density
    }

    private val cutoutPath = Path()
    private val frameRect = RectF()
    private val cornerRadius = FRAME_CORNER_RADIUS_DP * density

    /** The square area (in this view's coordinates) where a barcode/QR code should be placed. */
    val frameBounds: RectF get() = RectF(frameRect)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val (left, top) = ScanFrameCalculator().calculateScanFrameOffset(w, h)
        frameRect.set(left.toFloat(), top.toFloat(), (w - left).toFloat(), (h - top).toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        cutoutPath.reset()
        cutoutPath.addRect(0f, 0f, width.toFloat(), height.toFloat(), Path.Direction.CW)
        cutoutPath.addRoundRect(frameRect, cornerRadius, cornerRadius, Path.Direction.CCW)
        cutoutPath.fillType = Path.FillType.EVEN_ODD
        canvas.drawPath(cutoutPath, scrimPaint)

        canvas.drawRoundRect(frameRect, cornerRadius, cornerRadius, framePaint)
    }
}
