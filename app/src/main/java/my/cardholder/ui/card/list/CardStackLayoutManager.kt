package my.cardholder.ui.card.list

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * A grid that, in single-column mode, keeps cards scrolled past the top and bottom attached for a while,
 * so [CardStackScrollEffect] can still show and smoothly fade them in/out.
 */
class CardStackLayoutManager(context: Context) : GridLayoutManager(context, 1) {

    var extraStartSpacePx = 0
    var extraEndSpacePx = 0

    override fun calculateExtraLayoutSpace(state: RecyclerView.State, extraLayoutSpace: IntArray) {
        super.calculateExtraLayoutSpace(state, extraLayoutSpace)
        if (spanCount == 1) {
            // Space at the start and end is excluded from recycling while scrolling to allow smooth fading.
            extraLayoutSpace[0] = maxOf(extraLayoutSpace[0], extraStartSpacePx)
            extraLayoutSpace[1] = maxOf(extraLayoutSpace[1], extraEndSpacePx)
        }
    }
}
