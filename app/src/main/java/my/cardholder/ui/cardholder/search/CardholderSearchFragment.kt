package my.cardholder.ui.cardholder.search

import android.transition.TransitionInflater
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigator
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardholderSearchBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.ui.cardholder.cards.CardholderCardsListAdapter

@AndroidEntryPoint
class CardholderSearchFragment : BaseFragment<FragmentCardholderSearchBinding>(
    FragmentCardholderSearchBinding::inflate
) {

    override val viewModel: CardholderSearchViewModel by viewModels()

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
        binding.searchTextInputLayout.apply {
            editText?.doAfterTextChanged { viewModel.onSearchTextChanged(it?.toString()) }
        }
        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
        }
    }

    override fun collectData() {
        viewModel.state.collectWhenStarted { state ->
            when (state) {
                is CardholderSearchState.Empty ->  {
                    binding.searchEmptyMessageText.text = getString(state.messageRes)
                    listAdapter.submitList(null)
                }
                is CardholderSearchState.Success -> {
                    binding.searchEmptyMessageText.text = null
                    listAdapter.submitList(state.cards)
                }
            }
        }
    }
}
