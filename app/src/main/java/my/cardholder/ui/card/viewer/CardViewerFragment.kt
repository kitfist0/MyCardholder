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
        binding.cardEditFab.setOnClickListener {
            viewModel.onEditFabClicked()
        }
    }

    override fun collectData() {
        viewModel.card.collectWhenStarted { card ->
            ViewCompat.setTransitionName(binding.cardTitleText, card.titleTransitionId())
            ViewCompat.setTransitionName(binding.cardSubtitleText, card.textTransitionId())
            ViewCompat.setTransitionName(binding.cardBarcodeImage, card.barcodeTransitionId())
            binding.cardTitleText.text = card.title
            binding.cardSubtitleText.text = card.text
        }
    }
}
