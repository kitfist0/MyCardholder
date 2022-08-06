package my.cardholder.ui.cards

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardsBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class CardsFragment : BaseFragment<FragmentCardsBinding>(FragmentCardsBinding::inflate) {

    override val viewModel: CardsViewModel by viewModels()

    private val listAdapter by lazy {
        CardsListAdapter(
            onItemClick = { cardId, sharedElementsMap ->
                val extras = FragmentNavigator.Extras.Builder()
                    .addSharedElements(sharedElementsMap)
                    .build()
                viewModel.onCardClicked(cardId, extras)
            }
        )
    }

    override fun initViews() {
        binding.cardsRecyclerView.apply {
            addItemDecoration(CardsOverlapDecoration())
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listAdapter
        }
    }

    override fun collectData() {
        viewModel.cards.collectWhenStarted { cards -> listAdapter.submitList(cards) }
        viewModel.eventsFlow.collectWhenStarted { findNavController().navigate(it.first, it.second) }
    }
}
