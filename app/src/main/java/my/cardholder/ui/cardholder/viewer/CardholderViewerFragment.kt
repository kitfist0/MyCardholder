package my.cardholder.ui.cardholder.viewer

import android.transition.TransitionInflater
import android.view.View
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.data.model.Card.Companion.getBarcodeFile
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.databinding.FragmentCardholderViewerBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.assistedViewModels
import my.cardholder.util.ext.loadBarcodeImage
import my.cardholder.util.ext.setupUniqueTransitionName
import javax.inject.Inject

@AndroidEntryPoint
class CardholderViewerFragment : BaseFragment<FragmentCardholderViewerBinding>(
    FragmentCardholderViewerBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: CardholderViewerViewModelFactory

    private val args: CardholderViewerFragmentArgs by navArgs()

    override val viewModel: CardholderViewerViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            val uniqueNameSuffix = args.cardId
            cardViewerBarcodeImage.setupUniqueTransitionName(uniqueNameSuffix)
            cardViewerCardNameText.setupUniqueTransitionName(uniqueNameSuffix)
            cardViewerCardTextText.setupUniqueTransitionName(uniqueNameSuffix)
            cardViewerEditFab.setupUniqueTransitionName(uniqueNameSuffix)
            cardViewerEditFab.setOnClickListener {
                val sharedElements = with(binding) {
                    mapOf<View, String>(
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
            cardViewerDeleteCardButton.setOnClickListener {
                viewModel.onDeleteCardButtonClicked()
            }
        }
    }

    override fun collectData() {
        viewModel.state.collectWhenStarted { state ->
            when (state) {
                CardholderViewerState.Loading -> with(binding) {
                    cardViewerEditFab.isClickable = false
                }
                is CardholderViewerState.Success -> with(binding) {
                    val card = state.card
                    cardViewerBarcodeImage.apply {
                        setBackgroundColor(card.getColorInt())
                        loadBarcodeImage(card.getBarcodeFile(context))
                    }
                    cardViewerCardNameText.text = card.name
                    cardViewerCardTextText.text = card.text
                    cardViewerEditFab.isClickable = true
                }
            }
        }
    }
}
