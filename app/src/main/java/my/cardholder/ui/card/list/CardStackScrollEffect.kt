package my.cardholder.ui.card.list

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.EdgeEffect
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Wallet-like scrolling of the single-column card stack (cards overlap via [CardStackItemDecoration]):
 * - cards scrolled to the top gather into a compact pile there, showing the top edges of the last
 *   [MAX_VISIBLE_EDGES] cards, each a bit smaller, while older ones fade out;
 * - pulling the list down at its top spreads the cards apart, and they spring back on release.
 */
class CardStackScrollEffect(
    private val recyclerView: RecyclerView,
    private val overlapPx: Int,
    private val edgePx: Int,
    private val isDragged: (View) -> Boolean,
) : RecyclerView.OnScrollListener(),
    View.OnLayoutChangeListener,
    RecyclerView.OnChildAttachStateChangeListener {

    companion object {
        const val MAX_VISIBLE_EDGES = 3

        // How much smaller each card in the pile is than the one in front of it.
        private const val SCALE_STEP = 0.04f

        // Share of the pull distance turned into spreading the cards apart.
        private const val PULL_RESISTANCE = 0.5f

        // Extra gap that each next card gets per pixel of spread.
        private const val SPREAD_PER_CARD = 0.3f
        private const val MAX_SPREAD_DP = 120
        private const val SPREAD_RELEASE_DURATION_MS = 300L
        private const val SPREAD_ABSORB_DURATION_MS = 350L
        private const val ABSORB_VELOCITY_FACTOR = 0.03f
    }

    private val maxSpreadPx = MAX_SPREAD_DP * recyclerView.resources.displayMetrics.density
    private var spreadPx = 0f
    private var spreadAnimator: ValueAnimator? = null

    fun attach() {
        recyclerView.addOnScrollListener(this)
        // Layout passes (new data, column count changes) move cards without scrolling.
        recyclerView.addOnLayoutChangeListener(this)
        recyclerView.addOnChildAttachStateChangeListener(this)
        recyclerView.edgeEffectFactory = SpreadEdgeEffectFactory()
        // Lets the cards spread even when they all fit on the screen.
        recyclerView.overScrollMode = View.OVER_SCROLL_ALWAYS
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        update()
    }

    override fun onLayoutChange(
        v: View, left: Int, top: Int, right: Int, bottom: Int,
        oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int,
    ) {
        update()
    }

    override fun onChildViewAttachedToWindow(view: View) {}

    override fun onChildViewDetachedFromWindow(view: View) {
        // Views are recycled for other cards, so they must not keep a stacked position.
        reset(view)
    }

    private fun update() {
        val isSingleColumn = (recyclerView.layoutManager as? GridLayoutManager)?.spanCount == 1
        // The front card of the pile stops here; the edges of older cards are shown above it.
        val pileFrontTop = recyclerView.paddingTop + MAX_VISIBLE_EDGES * edgePx
        for (index in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(index)
            // ItemTouchHelper moves the dragged card itself.
            if (isDragged(child)) continue
            if (!isSingleColumn || child.height == 0) {
                reset(child)
                continue
            }
            val scrolledPast = pileFrontTop - child.top
            if (scrolledPast > 0) {
                // How many cards have already slid over this one (fractional while sliding).
                val coveredBy = scrolledPast.toFloat() / child.stackStride()
                val depth = coveredBy.coerceAtMost(MAX_VISIBLE_EDGES.toFloat())
                val scale = 1f - SCALE_STEP * depth
                child.pivotX = child.width / 2f
                child.pivotY = 0f
                child.translationY = pileFrontTop - edgePx * depth - child.top
                child.scaleX = scale
                child.scaleY = scale
                child.alpha = (1f - (coveredBy - MAX_VISIBLE_EDGES)).coerceIn(0f, 1f)
            } else {
                val position = recyclerView.getChildAdapterPosition(child).coerceAtLeast(0)
                child.translationY = spreadPx * SPREAD_PER_CARD * position
                child.scaleX = 1f
                child.scaleY = 1f
                child.alpha = 1f
            }
        }
        // Keeps cards attached until they have faded out of the pile.
        recyclerView.getChildAt(0)?.let { child ->
            (recyclerView.layoutManager as? CardStackLayoutManager)?.extraStartSpacePx =
                (MAX_VISIBLE_EDGES + 1) * child.stackStride()
        }
    }

    /** Distance between the tops of two neighbouring overlapping cards. */
    private fun View.stackStride(): Int {
        val margins = (layoutParams as? ViewGroup.MarginLayoutParams)
            ?.let { it.topMargin + it.bottomMargin } ?: 0
        return (height + margins - overlapPx).coerceAtLeast(1)
    }

    private fun reset(view: View) {
        view.translationY = 0f
        view.scaleX = 1f
        view.scaleY = 1f
        view.alpha = 1f
    }

    private fun setSpread(px: Float) {
        spreadPx = px.coerceIn(0f, maxSpreadPx)
        update()
    }

    private fun animateSpread(vararg values: Float, durationMs: Long) {
        spreadAnimator?.cancel()
        spreadAnimator = ValueAnimator.ofFloat(*values).apply {
            duration = durationMs
            interpolator = DecelerateInterpolator()
            addUpdateListener { setSpread(it.animatedValue as Float) }
            start()
        }
    }

    /** Replaces the overscroll glow/stretch at the top with spreading the cards apart. */
    private inner class SpreadEdgeEffectFactory : RecyclerView.EdgeEffectFactory() {

        override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
            if (direction != DIRECTION_TOP) {
                return super.createEdgeEffect(view, direction)
            }
            return object : EdgeEffect(view.context) {

                override fun onPull(deltaDistance: Float) {
                    pull(deltaDistance)
                }

                override fun onPull(deltaDistance: Float, displacement: Float) {
                    pull(deltaDistance)
                }

                @RequiresApi(Build.VERSION_CODES.S)
                override fun onPullDistance(deltaDistance: Float, displacement: Float): Float {
                    pull(deltaDistance)
                    return deltaDistance
                }

                @RequiresApi(Build.VERSION_CODES.S)
                override fun getDistance(): Float = spreadPx / view.height.coerceAtLeast(1)

                override fun onRelease() {
                    animateSpread(spreadPx, 0f, durationMs = SPREAD_RELEASE_DURATION_MS)
                }

                override fun onAbsorb(velocity: Int) {
                    val peak = (velocity * ABSORB_VELOCITY_FACTOR).coerceAtMost(maxSpreadPx / 2)
                    animateSpread(0f, peak, 0f, durationMs = SPREAD_ABSORB_DURATION_MS)
                }

                override fun isFinished(): Boolean = spreadPx == 0f

                // Nothing to draw: the cards themselves show the overscroll.
                override fun draw(canvas: Canvas): Boolean = false

                private fun pull(deltaDistance: Float) {
                    spreadAnimator?.cancel()
                    setSpread(spreadPx + deltaDistance * view.height * PULL_RESISTANCE)
                }
            }
        }
    }
}
