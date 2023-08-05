package my.cardholder.ui.card.display

import android.transition.TransitionInflater
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardDisplayBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.*
import javax.inject.Inject

@AndroidEntryPoint
class CardDisplayFragment : BaseFragment<FragmentCardDisplayBinding>(
    FragmentCardDisplayBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: CardDisplayViewModelFactory

    private val args: CardDisplayFragmentArgs by navArgs()

    override val viewModel: CardDisplayViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets(top = false)
            val uniqueNameSuffix = args.cardId
            cardDisplayBarcodeImage.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                setPadding(getStatusBarHeight())
            }
            cardDisplayCardCategoryText.setupUniqueTransitionName(uniqueNameSuffix)
            cardDisplayCardNameText.setupUniqueTransitionName(uniqueNameSuffix)
            cardDisplayCardContentText.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                val extras = listOf(cardDisplayCardContentText).toNavExtras()
                setOnLongClickListener {
                    viewModel.onCardContentTextLongClicked(extras)
                }
            }
            cardDisplayEditFab.setupUniqueTransitionName(uniqueNameSuffix)
            cardDisplayEditFab.setOnClickListener {
                val extras = listOf(
                    cardDisplayCardNameText,
                    cardDisplayCardContentText,
                    cardDisplayCardCategoryText,
                    cardDisplayEditFab,
                ).toNavExtras()
                viewModel.onEditFabClicked(extras)
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            with(binding) {
                when (state) {
                    is CardDisplayState.Loading -> {
                        cardDisplayCardCategoryText.isVisible = false
                        cardDisplayEditFab.isClickable = false
                    }
                    is CardDisplayState.Success -> {
                        cardDisplayBarcodeImage.apply {
                            setBackgroundColor(state.cardColor)
                            loadBarcodeImage(state.barcodeFile)
                        }
                        cardDisplayCardCategoryText.isVisible = state.cardCategory.isNotEmpty()
                        cardDisplayCardCategoryText.text = state.cardCategory
                        cardDisplayCardNameText.text = state.cardName
                        cardDisplayCardContentText.text = state.cardContent
                        cardDisplayEditFab.isClickable = true
                    }
                }
            }
        }
    }
}
