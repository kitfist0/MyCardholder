package my.cardholder.ui.card.viewer

import android.transition.TransitionInflater
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigator
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.data.Card.Companion.barcodeTransitionId
import my.cardholder.data.Card.Companion.fabTransitionId
import my.cardholder.data.Card.Companion.textTransitionId
import my.cardholder.data.Card.Companion.nameTransitionId
import my.cardholder.databinding.FragmentCardViewerBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class CardViewerFragment : BaseFragment<FragmentCardViewerBinding>(
    FragmentCardViewerBinding::inflate
) {

    private var sharedElementsMap = emptyMap<View, String>()

    override val menuRes = R.menu.menu_card_viewer

    override val viewModel: CardViewerViewModel by viewModels()

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        binding.cardViewerEditFab.setOnClickListener {
            val extras = FragmentNavigator.Extras.Builder()
                .addSharedElements(sharedElementsMap)
                .build()
            viewModel.onEditFabClicked(extras)
        }
    }

    override fun collectData() {
        viewModel.card.collectWhenStarted { card ->
            sharedElementsMap = mapOf(
                binding.cardViewerEditFab to card.fabTransitionId(),
                binding.cardViewerCardNameText to card.nameTransitionId(),
                binding.cardViewerCardTextText to card.textTransitionId(),
                binding.cardViewerBarcodeImage to card.barcodeTransitionId(),
            ).onEach { entry -> ViewCompat.setTransitionName(entry.key, entry.value) }
            binding.cardViewerCardNameText.text = card.name
            binding.cardViewerCardTextText.text = card.text
        }
    }
}
