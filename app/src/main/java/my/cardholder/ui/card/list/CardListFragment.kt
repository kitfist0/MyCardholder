package my.cardholder.ui.card.list

import android.transition.TransitionInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardListBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.ui.card.adapter.CardsAdapter
import my.cardholder.util.ext.collectWhenStarted
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
        CardsAdapter(
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
            cardListRecyclerView.apply {
                clipToPadding = false
                updateVerticalPaddingAfterApplyingWindowInsets(bottom = false)
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
            cardListSearchFab.setOnClickListener {
                val sharedElements = mapOf<View, String>(
                    cardListSearchFab to cardListSearchFab.transitionName,
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
                is CardListState.Empty -> {
                    binding.cardListSearchFab.isVisible = false
                    binding.cardListEmptyListMessageText.setText(state.messageRes)
                    listAdapter.submitList(null)
                }
                is CardListState.Success -> {
                    (binding.cardListRecyclerView.layoutManager as GridLayoutManager).apply {
                        val count = state.spanCount
                        if (spanCount != count) spanCount = count
                    }
                    binding.cardListSearchFab.isVisible = true
                    binding.cardListEmptyListMessageText.text = null
                    listAdapter.submitList(state.cards)
                }
            }
        }
    }
}
