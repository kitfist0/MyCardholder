package my.cardholder.ui.card.display

import android.transition.TransitionInflater
import android.transition.TransitionSet
import androidx.core.transition.doOnEnd
import androidx.core.transition.doOnStart
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardDisplayBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.*

@AndroidEntryPoint
class CardDisplayFragment : BaseFragment<FragmentCardDisplayBinding>(
    FragmentCardDisplayBinding::inflate
) {

    private val args: CardDisplayFragmentArgs by navArgs()

    override val viewModel: CardDisplayViewModel by viewModels()

    override fun initViews() {
        childFragmentManager.addFragmentOnAttachListener { _, _ ->
            binding.cardDisplayBarcodeImage.setPadding(size = getStatusBarHeight())
        }
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets(top = false)
            val uniqueNameSuffix = args.cardId
            cardDisplayBarcodeImage.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                setOnClickListener {
                    val extras = listOf(cardDisplayBarcodeImage).toNavExtras()
                    viewModel.onBarcodeImageClicked(extras)
                }
            }
            cardDisplayCardCategoryText.setupUniqueTransitionName(uniqueNameSuffix)
            cardDisplayCardNameText.setupUniqueTransitionName(uniqueNameSuffix)
            cardDisplayCardContentCardView.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                val extras = listOf(cardDisplayCardContentCardView).toNavExtras()
                setOnLongClickListener {
                    viewModel.onCardContentTextLongClicked(extras)
                }
            }
            cardDisplayEditFab.setupUniqueTransitionName(uniqueNameSuffix)
            cardDisplayEditFab.setOnClickListener {
                val extras = listOf(
                    cardDisplayCardNameText,
                    cardDisplayCardContentCardView,
                    cardDisplayCardCategoryText,
                    cardDisplayEditFab,
                ).toNavExtras()
                viewModel.onEditFabClicked(extras)
            }
            val transitionSet = sharedElementEnterTransition as TransitionSet
            transitionSet.doOnStart {
                cardDisplayCardLogoImage.isVisible = false
            }
            transitionSet.doOnEnd {
                cardDisplayCardLogoImage.animateVisibilityChange()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            with(binding) {
                when (state) {
                    is CardDisplayState.Loading -> {
                        cardDisplayCardLogoImage.setImageDrawable(null)
                        cardDisplayCardCategoryText.isVisible = false
                        cardDisplayEditFab.isClickable = false
                    }
                    is CardDisplayState.Success -> {
                        cardDisplayBarcodeImage.apply {
                            setBackgroundColor(state.cardColor)
                            loadBarcodeImage(state.barcodeFile)
                        }
                        cardDisplayExplanationMessageText.isVisible = state.explanationIsVisible
                        cardDisplayCardCategoryText.text = state.cardCategory
                        cardDisplayCardCategoryText.isVisible = state.cardCategory.isNotEmpty()
                        cardDisplayCardLogoImage.loadLogoImage(state.cardLogo)
                        cardDisplayCardNameText.text = state.cardName
                        cardDisplayCardContentText.text = state.cardContent
                        cardDisplayEditFab.isClickable = true
                    }
                }
            }
        }
    }
}
