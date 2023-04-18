package my.cardholder.ui.search

import android.content.Context
import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.view.inputmethod.InputMethodManager
import androidx.core.transition.doOnEnd
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigator
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardholderSearchBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.ui.adapter.CardholderAdapter
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class SearchFragment : BaseFragment<FragmentCardholderSearchBinding>(
    FragmentCardholderSearchBinding::inflate
) {

    override val viewModel: SearchViewModel by viewModels()

    private val listAdapter by lazy {
        CardholderAdapter(
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
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            searchTextInputLayout.editText
                ?.doAfterTextChanged { viewModel.onSearchTextChanged(it?.toString()) }
            searchResultsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
            }
            searchTextInputLayout.requestFocus()
            (sharedElementEnterTransition as TransitionSet).doOnEnd {
                (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                    ?.showSoftInput(searchTextInputLayout.editText, 1)
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is SearchState.Empty -> {
                    binding.searchEmptyMessageText.text = getString(state.messageRes)
                    listAdapter.submitList(null)
                }
                is SearchState.Success -> {
                    binding.searchEmptyMessageText.text = null
                    listAdapter.submitList(state.cards)
                }
            }
        }
    }
}
