package my.cardholder.ui.card.viewer

import android.transition.TransitionInflater
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.navArgs
import coil.load
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.data.model.Card.Companion.getBarcodeFile
import my.cardholder.databinding.FragmentCardViewerBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.assistedViewModels
import my.cardholder.util.setupUniqueTransitionNamesAndReturnSharedElements
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
        val sharedElements = binding.setupUniqueTransitionNamesAndReturnSharedElements(
            uniqueSuffix = args.cardId,
            R.id.card_viewer_card_name_text,
            R.id.card_viewer_card_text_text,
            R.id.card_viewer_barcode_image,
            R.id.card_viewer_edit_fab,
        )
        binding.cardViewerEditFab.setOnClickListener {
            val extras = FragmentNavigator.Extras.Builder()
                .addSharedElements(sharedElements)
                .build()
            viewModel.onEditFabClicked(extras)
        }
    }

    override fun collectData() {
        viewModel.card.collectWhenStarted { card ->
            binding.cardViewerBarcodeImage.load(card.getBarcodeFile(requireContext()))
            binding.cardViewerCardNameText.text = card.name
            binding.cardViewerCardTextText.text = card.text
        }
    }
}
