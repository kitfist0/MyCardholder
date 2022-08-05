package my.cardholder.ui.cards

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import my.cardholder.R

class CardsOverlapDecoration : RecyclerView.ItemDecoration() {

    private var topOverlap: Int = 0

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val itemPosition = parent.getChildAdapterPosition(view)
        if (itemPosition == 0) {
            return
        }
        if (topOverlap == 0) {
            topOverlap = view.context.resources
                .getDimensionPixelSize(R.dimen.cards_list_item_height)
        }
        outRect.set(0, topOverlap.inv().div(2), 0, 0)
    }
}
