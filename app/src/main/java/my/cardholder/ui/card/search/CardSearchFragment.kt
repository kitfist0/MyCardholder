package my.cardholder.ui.card.search

import android.animation.ValueAnimator
import android.graphics.drawable.ColorDrawable
import android.transition.TransitionInflater
import android.transition.TransitionSet
import androidx.core.transition.doOnEnd
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.FragmentCardSearchBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.showSoftInput
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class CardSearchFragment : BaseFragment<FragmentCardSearchBinding>(
    FragmentCardSearchBinding::inflate
) {

    override val viewModel: CardSearchViewModel by viewModels()

    private val cardSearchCategoryAdapter by lazy(LazyThreadSafetyMode.NONE) {
        CardSearchCategoryAdapter(
            onCategoryClicked = { categoryName ->
                viewModel.onCategoryItemClicked(categoryName)
            },
            onHeaderClicked = {
                viewModel.onHeaderItemClicked()
            }
        )
    }

    private val cardSearchResultAdapter by lazy(LazyThreadSafetyMode.NONE) {
        CardSearchResultAdapter(
            onItemClicked = { cardId, extras ->
                viewModel.onCardClicked(cardId, extras)
            }
        )
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            cardSearchTextInputLayout.editText
                ?.doAfterTextChanged { viewModel.onSearchTextChanged(it?.toString()) }
            cardSearchCategoryRecyclerView.apply {
                layoutManager = FlexboxLayoutManager(context)
                adapter = cardSearchCategoryAdapter
            }
            cardSearchResultRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = cardSearchResultAdapter
            }
        }
        val colorDrawable = binding.root.background as? ColorDrawable
        (sharedElementEnterTransition as TransitionSet).doOnEnd {
            val animator = ValueAnimator.ofObject(
                ArgbEvaluatorCompat(),
                colorDrawable?.color,
                MaterialColors.getColor(binding.root, android.R.attr.windowBackground)
            )
            animator.addUpdateListener { view?.setBackgroundColor(it.animatedValue as Int) }
            animator.start()
            binding.cardSearchTextInputLayout.showSoftInput()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CardSearchState.SearchInAllCategories -> {
                    binding.cardSearchTextInputLayout.editText?.hint =
                        getString(R.string.card_search_in_all_categories_hint)
                    binding.cardSearchNothingFoundText.isVisible = false
                    cardSearchCategoryAdapter.submitList(state.categories)
                    cardSearchResultAdapter.submitList(null)
                }
                is CardSearchState.SearchInCategory -> {
                    binding.cardSearchTextInputLayout.editText?.hint =
                        getString(R.string.card_search_within_category_hint).format(state.categoryName)
                    binding.cardSearchNothingFoundText.isVisible = false
                    cardSearchCategoryAdapter.submitList(null)
                    cardSearchResultAdapter.submitList(state.categoryCards)
                }
                is CardSearchState.NothingFound -> {
                    binding.cardSearchNothingFoundText.isVisible = true
                    cardSearchCategoryAdapter.submitList(null)
                    cardSearchResultAdapter.submitList(null)
                }
                is CardSearchState.Success -> {
                    binding.cardSearchNothingFoundText.isVisible = false
                    cardSearchCategoryAdapter.submitList(null)
                    cardSearchResultAdapter.submitList(state.cards)
                }
            }
        }
    }
}
