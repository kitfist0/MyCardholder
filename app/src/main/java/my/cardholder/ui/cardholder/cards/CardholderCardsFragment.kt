package my.cardholder.ui.cardholder.cards

import android.transition.TransitionInflater
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
        binding.cardsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listAdapter
            postponeEnterTransition()
            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
    }

    override fun collectData() {
        viewModel.cards.collectWhenStarted { cards -> listAdapter.submitList(cards) }
    }
}
