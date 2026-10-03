package my.cardholder.ui.card.display

import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.view.View
import android.widget.TextView
import androidx.core.app.SharedElementCallback
import androidx.core.transition.doOnEnd
import androidx.core.transition.doOnStart
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.FragmentCardDisplayBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.*

@AndroidEntryPoint
class CardDisplayFragment : BaseFragment<FragmentCardDisplayBinding>(
    FragmentCardDisplayBinding::inflate
) {

    private val args: CardDisplayFragmentArgs by navArgs()

    override val viewModel: CardDisplayViewModel by viewModels()

    // A three-column card list doesn't show the card content, so it can't move from there.
    private var isCardContentSharedElement = true

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets(top = false)
            val uniqueNameSuffix = args.cardId
            cardDisplayBarcodeImage.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                updateVerticalPaddingAfterApplyingWindowInsets()
                setOnClickListener {
                    val extras = listOf(cardDisplayBarcodeImage).toNavExtras()
                    viewModel.onBarcodeImageClicked(extras)
                }
            }
            cardDisplayToolbar.setOnMenuItemClickListener { menuItem ->
                viewModel.onToolbarMenuItemClicked(menuItem.itemId)
                true
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
            cardDisplayCardCommentText.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                val extras = listOf(cardDisplayCardCommentText).toNavExtras()
                setOnLongClickListener {
                    viewModel.onCardCommentTextLongClicked(extras)
                }
            }
            cardDisplayEditFab.setupUniqueTransitionName(uniqueNameSuffix)
            cardDisplayEditFab.setOnClickListener {
                val extras = listOf(
                    cardDisplayBarcodeImage,
                    cardDisplayCardNameText,
                    cardDisplayCardContentCardView,
                    cardDisplayCardCategoryText,
                    cardDisplayCardCommentText,
                    cardDisplayEditFab,
                ).toNavExtras()
                viewModel.onEditFabClicked(extras)
            }
            setEnterSharedElementCallback(
                object : SharedElementCallback() {
                    override fun onMapSharedElements(names: List<String>, sharedElements: Map<String, View>) {
                        isCardContentSharedElement = cardDisplayCardContentCardView.transitionName in names
                    }
                }
            )
            val transitionSet = sharedElementEnterTransition as TransitionSet
            transitionSet.doOnStart {
                cardDisplayToolbar.isVisible = false
                cardDisplayCardLogoImage.isVisible = false
                // Invisible rather than gone, so the views below it stay in place.
                if (!isCardContentSharedElement) cardDisplayCardContentCardView.isInvisible = true
            }
            transitionSet.doOnEnd {
                cardDisplayToolbar.animateVisibilityChange()
                cardDisplayCardLogoImage.animateVisibilityChange()
                if (!isCardContentSharedElement) cardDisplayCardContentCardView.animateVisibilityChange()
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
                        cardDisplayCardCategoryText.isVisible = true
                        cardDisplayCardCategoryText.setCardCategoryTextAndBackground(state.cardCategory)
                        cardDisplayCardLogoImage.loadLogoImage(
                            logoUrl = state.cardLogo,
                            defaultDrawableRes = R.drawable.ic_broken_img,
                        )
                        cardDisplayCardNameText.text = state.cardName
                        cardDisplayCardContentText.text = state.cardContent
                        cardDisplayCardCommentText.text = state.cardComment
                        cardDisplayEditFab.isClickable = true
                    }
                }
            }
        }
    }

    private fun TextView.setCardCategoryTextAndBackground(cardCategory: String?) {
        val noCategory = cardCategory.isNullOrEmpty()
        text = if (noCategory) getString(R.string.card_display_no_category) else cardCategory
        setBackgroundResource(
            if (noCategory) R.drawable.bg_error_container else R.drawable.bg_secondary_container
        )
    }
}
