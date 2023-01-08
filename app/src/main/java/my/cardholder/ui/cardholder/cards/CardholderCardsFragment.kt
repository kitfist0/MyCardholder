package my.cardholder.ui.cardholder.cards

import android.transition.TransitionInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigator
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardholderCardsBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class CardholderCardsFragment : BaseFragment<FragmentCardholderCardsBinding>(
    FragmentCardholderCardsBinding::inflate
) {

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
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
                postponeEnterTransition()
                viewTreeObserver.addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
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
        viewModel.state.collectWhenStarted { state ->
            when (state) {
                is CardholderCardsState.Empty -> {
                    listAdapter.submitList(null)
                    binding.cardsSearchFab.isVisible = false
                    binding.cardsEmptyListText.setText(state.messageRes)
                }
                is CardholderCardsState.Success -> {
                    listAdapter.submitList(state.cards)
                    binding.cardsSearchFab.isVisible = true
                    binding.cardsEmptyListText.text = null
                }
            }
        }
    }
}
