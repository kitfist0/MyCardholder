package my.cardholder.ui.card.list

import android.transition.TransitionInflater
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.FragmentCardListBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.getActionBarSize
import my.cardholder.util.ext.toNavExtras
import my.cardholder.util.ext.updateSpanCountIfRequired
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class CardListFragment : BaseFragment<FragmentCardListBinding>(
    FragmentCardListBinding::inflate
) {

    private companion object {
        const val VERTICAL_SCROLL_THRESHOLD = 10
    }

    override val viewModel: CardListViewModel by viewModels()

    private val listAdapter by lazy(LazyThreadSafetyMode.NONE) {
        CardListAdapter(
            onItemClicked = { cardId, extras -> viewModel.onCardClicked(cardId, extras) },
            onItemCountIncreased = { binding.cardListRecyclerView.smoothScrollToPosition(0) },
        )
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            cardListRecyclerView.apply {
                clipToPadding = false
                setHasFixedSize(true)
                updateVerticalPaddingAfterApplyingWindowInsets(bottom = false)
                layoutManager = CardStackLayoutManager(context)
                val overlapPx = resources.getDimensionPixelSize(R.dimen.card_item_stack_overlap)
                val edgePx = resources.getDimensionPixelSize(R.dimen.card_item_stack_edge)
                addItemDecoration(
                    CardStackItemDecoration(
                        overlapPx = overlapPx,
                        stackTopSpacePx = CardStackScrollEffect.MAX_VISIBLE_EDGES * edgePx,
                    )
                )
                adapter = listAdapter
                listAdapter.attachToRecyclerView(this)
                CardStackScrollEffect(
                    recyclerView = this,
                    overlapPx = overlapPx,
                    edgePx = edgePx,
                    isDragged = listAdapter::isDragged,
                ).attach()
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
                                    cardListSearchFab.show()
                                // if the recycler view is scrolled above hide the FAB
                                dy > VERTICAL_SCROLL_THRESHOLD && cardListSearchFab.isShown ->
                                    cardListSearchFab.hide()
                                // if the recycler view is scrolled above show the FAB
                                dy < -VERTICAL_SCROLL_THRESHOLD && !cardListSearchFab.isShown ->
                                    cardListSearchFab.show()
                            }
                        }
                    }
                )
            }
            cardListImportCardsFab.setOnClickListener {
                viewModel.onImportCardsFabClicked()
            }
            cardListSearchFab.setOnClickListener {
                val extras = listOf(cardListSearchFab).toNavExtras()
                viewModel.onSearchFabClicked(extras)
            }
            cardListToolbar.apply {
                val toolbarLayoutParams = cardListToolbar.layoutParams
                toolbarLayoutParams.height = (getActionBarSize() / 2)
                cardListToolbar.layoutParams = toolbarLayoutParams
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CardListState.Empty -> {
                    binding.cardListSearchFab.isVisible = false
                    binding.cardListImportCardsFab.isVisible = true
                    binding.cardListEmptyListMessageText.setText(state.messageRes)
                    listAdapter.submitList(null)
                }

                is CardListState.Success -> {
                    binding.cardListRecyclerView.updateSpanCountIfRequired(state.spanCount)
                    listAdapter.isSingleColumn = state.spanCount == 1
                    binding.cardListSearchFab.isVisible = true
                    binding.cardListImportCardsFab.isVisible = false
                    binding.cardListEmptyListMessageText.text = null
                    listAdapter.submitList(state.cardsAndCategories) {
                        if (state.scrollUpEvent) {
                            binding.cardListRecyclerView.scrollToPosition(0)
                            viewModel.consumeScrollUpEvent()
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.updateCardPositions(listAdapter.currentList)
    }
}
