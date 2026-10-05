package my.cardholder.ui.card.list

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.card.MaterialCardView
import kotlin.math.roundToInt

/**
 * A card in the card list that, with [hasRealProportions] set, is as high as a real bank card
 * (ISO/IEC 7810 ID-1, 85.60 × 53.98 mm) of the same width. It never gets lower than its content.
 */
class CardItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : MaterialCardView(context, attrs) {

    companion object {
        private const val REAL_ASPECT_RATIO = 85.60f / 53.98f

        fun realHeightForWidth(width: Int): Int = (width / REAL_ASPECT_RATIO).roundToInt()
    }

    var hasRealProportions = false
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (hasRealProportions) {
            val realHeight = realHeightForWidth(measuredWidth)
            if (realHeight > measuredHeight) {
                super.onMeasure(
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(realHeight, MeasureSpec.EXACTLY)
                )
            }
        }
    }
}
