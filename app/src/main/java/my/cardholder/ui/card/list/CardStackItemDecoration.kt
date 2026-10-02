package my.cardholder.ui.card.list

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * In a single-column list, pulls every card up over the bottom of the previous one, so the cards
 * always lie in a stack like real ones, and leaves room above the first card for the edges of
 * the cards stacked at the top while scrolling (see [CardStackScrollEffect]).
 */
class CardStackItemDecoration(
    private val overlapPx: Int,
    private val stackTopSpacePx: Int,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val isSingleColumn = (parent.layoutManager as? GridLayoutManager)?.spanCount == 1
        val position = parent.getChildAdapterPosition(view)
        when {
            !isSingleColumn -> outRect.setEmpty()
            position == 0 -> outRect.set(0, stackTopSpacePx, 0, 0)
            else -> outRect.set(0, -overlapPx, 0, 0)
        }
    }
}
