package my.cardholder.ui.card.search

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.view.inputmethod.InputMethodManager
import androidx.core.transition.doOnEnd
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.google.android.material.color.MaterialColors
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardSearchBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class CardSearchFragment : BaseFragment<FragmentCardSearchBinding>(
    FragmentCardSearchBinding::inflate
) {

    override val viewModel: CardSearchViewModel by viewModels()

    private val listAdapter by lazy(LazyThreadSafetyMode.NONE) {
        CardSearchAdapter(
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
            cardSearchResultsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
            }
            cardSearchTextInputLayout.requestFocus()
            (sharedElementEnterTransition as TransitionSet).doOnEnd {
                (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                    ?.showSoftInput(cardSearchTextInputLayout.editText, 1)
            }
        }
        val transitionSet = sharedElementEnterTransition as TransitionSet
        val colorDrawable = binding.root.background as? ColorDrawable
        transitionSet.doOnEnd {
            val animator = ValueAnimator.ofObject(
                ArgbEvaluatorCompat(),
                colorDrawable?.color,
                MaterialColors.getColor(binding.root, android.R.attr.windowBackground)
            )
            animator.addUpdateListener { view?.setBackgroundColor(it.animatedValue as Int) }
            animator.start()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CardSearchState.Blank -> {
                    binding.cardSearchEmptyMessageText.text = state.message
                    listAdapter.submitList(null)
                }
                is CardSearchState.NothingFound -> {
                    binding.cardSearchEmptyMessageText.setText(state.messageRes)
                    listAdapter.submitList(null)
                }
                is CardSearchState.Success -> {
                    binding.cardSearchEmptyMessageText.text = null
                    listAdapter.submitList(state.cards)
                }
            }
        }
    }
}
