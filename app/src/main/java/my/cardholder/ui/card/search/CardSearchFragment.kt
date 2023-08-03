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
            cardSearchResultRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = cardSearchResultAdapter
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
                is CardSearchState.Default -> {
                    binding.cardSearchEmptyMessageText.text = state.message
                    cardSearchResultAdapter.submitList(null)
                }
                is CardSearchState.NothingFound -> {
                    binding.cardSearchEmptyMessageText.setText(state.messageRes)
                    cardSearchResultAdapter.submitList(null)
                }
                is CardSearchState.Success -> {
                    binding.cardSearchEmptyMessageText.text = null
                    cardSearchResultAdapter.submitList(state.cards)
                }
            }
        }
    }
}
