package my.cardholder.ui.card

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.data.Card.Companion.barcodeTransitionId
import my.cardholder.data.Card.Companion.textTransitionId
import my.cardholder.data.Card.Companion.titleTransitionId
import my.cardholder.databinding.FragmentCardBinding

@AndroidEntryPoint
class CardFragment : Fragment() {

    private val viewModel: CardViewModel by viewModels()

    private var _binding: FragmentCardBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        viewModel.card.observe(viewLifecycleOwner) { card ->
            ViewCompat.setTransitionName(binding.cardTitleText, card.titleTransitionId())
            ViewCompat.setTransitionName(binding.cardSubtitleText, card.textTransitionId())
            ViewCompat.setTransitionName(binding.cardBarcodeImage, card.barcodeTransitionId())
            binding.cardTitleText.text = card.title
            binding.cardSubtitleText.text = card.text
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
