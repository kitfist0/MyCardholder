package my.cardholder.ui.card.viewer

import android.transition.TransitionInflater
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.FragmentCardViewerBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.assistedViewModels
import javax.inject.Inject

@AndroidEntryPoint
class CardViewerFragment : BaseFragment<FragmentCardViewerBinding>(
    FragmentCardViewerBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: CardViewerViewModelFactory

    private val args: CardViewerFragmentArgs by navArgs()

    override val menuRes = R.menu.menu_card_viewer

    override val viewModel: CardViewerViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        val sharedElementsMap = mapOf(
            binding.cardViewerCardNameText to "trans_name_${args.cardId}",
            binding.cardViewerCardTextText to "trans_text_${args.cardId}",
            binding.cardViewerBarcodeImage to "trans_barcode_${args.cardId}",
            binding.cardViewerEditFab to "trans_fab_${args.cardId}",
        ).onEach { entry ->
            ViewCompat.setTransitionName(entry.key, entry.value)
        }
        binding.cardViewerEditFab.setOnClickListener {
            val extras = FragmentNavigator.Extras.Builder()
                .addSharedElements(sharedElementsMap)
                .build()
            viewModel.onEditFabClicked(extras)
        }
    }

    override fun collectData() {
        viewModel.card.collectWhenStarted { card ->
            binding.cardViewerCardNameText.text = card.name
            binding.cardViewerCardTextText.text = card.text
        }
    }
}
