package my.cardholder.ui.card.zoom

import android.content.res.ColorStateList
import android.os.Bundle
import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.transition.doOnEnd
import androidx.core.transition.doOnStart
import androidx.core.view.MenuItemCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardZoomBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.ui.card.CardSystemBarsAppearance
import my.cardholder.util.ContentColor
import my.cardholder.util.ext.animateVisibilityChange
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.loadBarcodeImage
import my.cardholder.util.ext.setupUniqueTransitionName
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class CardZoomFragment : BaseFragment<FragmentCardZoomBinding>(
    FragmentCardZoomBinding::inflate
) {

    private val args: CardZoomFragmentArgs by navArgs()

    override val viewModel: CardZoomViewModel by viewModels()

    // The card color fills the whole screen, under both system bars
    private val systemBarsAppearance = CardSystemBarsAppearance(this, isNavigationBarOnCard = true)

    private var prevBrightness = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        prevBrightness = requireActivity().window.attributes.screenBrightness
        setScreenBrightness(1f)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        setScreenBrightness(prevBrightness)
        super.onDestroyView()
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            val uniqueNameSuffix = args.cardId
            cardZoomToolbar.updateVerticalPaddingAfterApplyingWindowInsets(bottom = false)
            cardZoomToolbar.setOnMenuItemClickListener { menuItem ->
                viewModel.onToolbarMenuItemClicked(menuItem.itemId)
                true
            }
            cardZoomBarcodeImage.setupUniqueTransitionName(uniqueNameSuffix)
            val transitionSet = sharedElementEnterTransition as TransitionSet
            transitionSet.doOnStart {
                cardZoomToolbar.isVisible = false
            }
            transitionSet.doOnEnd {
                cardZoomToolbar.animateVisibilityChange()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CardZoomState.Loading ->
                    binding.cardZoomBarcodeImage.isVisible = false
                is CardZoomState.Success -> with(binding) {
                    cardZoomBarcodeImage.apply {
                        isVisible = true
                        setBackgroundColor(state.cardColor)
                        loadBarcodeImage(state.barcodeFile)
                    }
                    systemBarsAppearance.setCardColor(state.cardColor)
                    // The close icon lies on the card color, which may be light or dark in either app theme
                    val iconTint = ColorStateList.valueOf(ContentColor.on(state.cardColor, ContentColor.ICON_CONTRAST))
                    cardZoomToolbar.menu.forEach { item -> MenuItemCompat.setIconTintList(item, iconTint) }
                }
            }
        }
    }

    private fun setScreenBrightness(brightness: Float) {
        activity?.let {
            val attributes = it.window.attributes
            attributes.screenBrightness = brightness
            it.window.attributes = attributes
        }
    }
}
