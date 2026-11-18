package my.cardholder.ui.card.list

import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardStackItemDecorationTest {

    private companion object {
        const val WIDTH = 1080
        const val HEIGHT = 2000
        const val STRIDE = 420
    }

    @Test
    fun cardsStayStackedWhenListLaysThemOutWhileMeasuringItself() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val recyclerView = createRecyclerView()

            // Like a parent measuring the list with a non exact size first, e.g. while recreating the screen:
            // a list that measures itself lays its cards out before it knows its own width
            recyclerView.measure(
                MeasureSpec.makeMeasureSpec(WIDTH, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(HEIGHT, MeasureSpec.AT_MOST),
            )
            recyclerView.measure(
                MeasureSpec.makeMeasureSpec(WIDTH, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(HEIGHT, MeasureSpec.EXACTLY),
            )
            recyclerView.layout(0, 0, WIDTH, HEIGHT)

            val tops = (0 until 4).map { recyclerView.getChildAt(it).top }
            assertEquals("Each next card lies $STRIDE px lower: $tops", listOf(STRIDE, STRIDE, STRIDE), tops.zipWithNext { a, b -> b - a })
        }
    }

    private fun createRecyclerView(): RecyclerView {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        return RecyclerView(context).apply {
            layoutManager = CardStackLayoutManager(context)
            addItemDecoration(CardStackItemDecoration(stridePx = STRIDE, stackTopSpacePx = 0))
            adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                    object : RecyclerView.ViewHolder(
                        // A card as high as a real one of the list width, like CardItemView
                        View(context).apply {
                            layoutParams = ViewGroup.MarginLayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                CardItemView.realHeightForWidth(WIDTH),
                            )
                        }
                    ) {}

                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}
                override fun getItemCount() = 6
            }
        }
    }
}
