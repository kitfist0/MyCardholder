package my.cardholder.ui.cards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardsBinding

@AndroidEntryPoint
class CardsFragment : Fragment() {

    private val viewModel: CardsViewModel by viewModels()

    private var _binding: FragmentCardsBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    private val listAdapter by lazy {
        CardsListAdapter(
            onItemClick = { cardId -> viewModel.onCardClicked(cardId) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardsBinding.inflate(inflater, container, false)
        binding.cardsRecyclerView.apply {
            addItemDecoration(CardsOverlapDecoration())
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listAdapter
        }
        viewModel.cards.observe(viewLifecycleOwner) { cards ->
            listAdapter.submitList(cards)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
