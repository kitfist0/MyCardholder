package my.cardholder.ui.card.viewer

import android.transition.TransitionInflater
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.data.Card.Companion.barcodeTransitionId
import my.cardholder.data.Card.Companion.textTransitionId
import my.cardholder.data.Card.Companion.titleTransitionId
import my.cardholder.databinding.FragmentCardViewerBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class CardViewerFragment : BaseFragment<FragmentCardViewerBinding>(
    FragmentCardViewerBinding::inflate
) {

    override val viewModel: CardViewerViewModel by viewModels()

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        binding.cardViewerEditFab.setOnClickListener {
            viewModel.onEditFabClicked()
        }
    }

    override fun collectData() {
        viewModel.card.collectWhenStarted { card ->
            ViewCompat.setTransitionName(binding.cardViewerCardNameText, card.titleTransitionId())
            ViewCompat.setTransitionName(binding.cardViewerCardTextText, card.textTransitionId())
            ViewCompat.setTransitionName(binding.cardViewerBarcodeImage, card.barcodeTransitionId())
            binding.cardViewerCardNameText.text = card.title
            binding.cardViewerCardTextText.text = card.text
        }
    }
}
