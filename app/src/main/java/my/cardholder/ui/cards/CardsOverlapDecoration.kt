package my.cardholder.ui.cards

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CardsOverlapDecoration : RecyclerView.ItemDecoration() {

    companion object {
        private const val vertOverlap = -50
    }

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
        outRect.set(0, vertOverlap, 0, 0)
    }
}
