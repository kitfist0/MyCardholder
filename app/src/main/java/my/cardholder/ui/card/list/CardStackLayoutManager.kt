package my.cardholder.ui.card.list

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * A grid that, in single-column mode, keeps cards scrolled past the top attached for a while, so
 * [CardStackScrollEffect] can still show them in the stack at the top of the list.
 */
class CardStackLayoutManager(context: Context) : GridLayoutManager(context, 1) {

    var extraStartSpacePx = 0

    override fun calculateExtraLayoutSpace(state: RecyclerView.State, extraLayoutSpace: IntArray) {
        super.calculateExtraLayoutSpace(state, extraLayoutSpace)
        if (spanCount == 1) {
            // Space at the start is also excluded from recycling while scrolling towards the end.
            extraLayoutSpace[0] = maxOf(extraLayoutSpace[0], extraStartSpacePx)
        }
    }
}
