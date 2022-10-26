package my.cardholder.ui.card.viewer

import android.transition.TransitionInflater
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.navArgs
import coil.load
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.data.model.Card.Companion.getBarcodeFile
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.databinding.FragmentCardViewerBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.assistedViewModels
import my.cardholder.util.setupUniqueTransitionNames
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
        with(binding) {
            setupUniqueTransitionNames(
                uniqueSuffix = args.cardId,
                cardViewerBarcodeImage,
                cardViewerCardNameText,
                cardViewerCardTextText,
                cardViewerEditFab,
            )
            cardViewerEditFab.setOnClickListener {
                val sharedElements = with(binding) {
                    mapOf(
                        cardViewerBarcodeImage to cardViewerBarcodeImage.transitionName,
                        cardViewerCardNameText to cardViewerCardNameText.transitionName,
                        cardViewerCardTextText to cardViewerCardTextText.transitionName,
                        cardViewerEditFab to cardViewerEditFab.transitionName,
                    )
                }
                val extras = FragmentNavigator.Extras.Builder()
                    .addSharedElements(sharedElements)
                    .build()
                viewModel.onEditFabClicked(extras)
            }
        }
    }

    override fun collectData() {
        viewModel.card.collectWhenStarted { card ->
            with(binding) {
                cardViewerBackgroundColorView.setBackgroundColor(card.getColorInt(requireContext()))
                cardViewerBarcodeImage.load(card.getBarcodeFile(requireContext()))
                cardViewerCardNameText.text = card.name
                cardViewerCardTextText.text = card.text
            }
        }
    }
}
