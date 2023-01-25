package my.cardholder.ui.cardholder.cards

import android.transition.TransitionInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardholderCardsBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class CardholderCardsFragment : BaseFragment<FragmentCardholderCardsBinding>(
    FragmentCardholderCardsBinding::inflate
) {

    private companion object {
        const val VERTICAL_SCROLL_THRESHOLD = 10
    }

    override val viewModel: CardholderCardsViewModel by viewModels()

    private val listAdapter by lazy {
        CardholderCardsListAdapter(
            onItemClick = { cardId, sharedElements ->
                val extras = FragmentNavigator.Extras.Builder()
                    .addSharedElements(sharedElements)
                    .build()
                viewModel.onCardClicked(cardId, extras)
            }
        )
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            cardsRecyclerView.apply {
                layoutManager = GridLayoutManager(context, 1)
                adapter = listAdapter
                postponeEnterTransition()
                viewTreeObserver.addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
                addOnScrollListener(
                    object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            when {
                                // if the recycler view is at the first item always show the FAB
                                !recyclerView.canScrollVertically(-1) ->
                                    cardsSearchFab.show()
                                // if the recycler view is scrolled above hide the FAB
                                dy > VERTICAL_SCROLL_THRESHOLD && cardsSearchFab.isShown ->
                                    cardsSearchFab.hide()
                                // if the recycler view is scrolled above show the FAB
                                dy < -VERTICAL_SCROLL_THRESHOLD && !cardsSearchFab.isShown ->
                                    cardsSearchFab.show()
                            }
                        }
                    }
                )
            }
            cardsSearchFab.setOnClickListener {
                val sharedElements = mapOf<View, String>(
                    cardsSearchFab to cardsSearchFab.transitionName,
                )
                val extras = FragmentNavigator.Extras.Builder()
                    .addSharedElements(sharedElements)
                    .build()
                viewModel.onSearchFabClicked(extras)
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CardholderCardsState.Empty -> {
                    listAdapter.submitList(null)
                    binding.cardsSearchFab.isVisible = false
                    binding.cardsEmptyListText.setText(state.messageRes)
                }
                is CardholderCardsState.Success -> {
                    (binding.cardsRecyclerView.layoutManager as GridLayoutManager).apply {
                        val count = state.spanCount
                        if (spanCount != count) spanCount = count
                    }
                    listAdapter.submitList(state.cards)
                    binding.cardsSearchFab.isVisible = true
                    binding.cardsEmptyListText.text = null
                }
            }
        }
    }
}
