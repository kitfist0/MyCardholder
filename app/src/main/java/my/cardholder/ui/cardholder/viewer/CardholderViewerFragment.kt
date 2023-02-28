package my.cardholder.ui.cardholder.viewer

import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.view.View
import androidx.core.transition.doOnEnd
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardholderViewerBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.*
import javax.inject.Inject

@AndroidEntryPoint
class CardholderViewerFragment : BaseFragment<FragmentCardholderViewerBinding>(
    FragmentCardholderViewerBinding::inflate
) {

    private companion object {
        const val DELETE_BUTTON_ANIM_DURATION = 300L
    }

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
            cardViewerCardContentText.setupUniqueTransitionName(uniqueNameSuffix)
            cardViewerEditFab.setupUniqueTransitionName(uniqueNameSuffix)
            cardViewerEditFab.setOnClickListener {
                val sharedElements = mapOf<View, String>(
                    cardViewerCardNameText to cardViewerCardNameText.transitionName,
                    cardViewerCardContentText to cardViewerCardContentText.transitionName,
                    cardViewerEditFab to cardViewerEditFab.transitionName,
                )
                val extras = FragmentNavigator.Extras.Builder()
                    .addSharedElements(sharedElements)
                    .build()
                viewModel.onEditFabClicked(extras)
            }
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            cardViewerDeleteCardButton.alpha = 0f
            (sharedElementEnterTransition as TransitionSet).doOnEnd {
                cardViewerDeleteCardButton.animate()
                    .setDuration(DELETE_BUTTON_ANIM_DURATION)
                    .alpha(1f)
                    .start()
                cardViewerDeleteCardButton.setOnClickListener {
                    viewModel.onDeleteCardButtonClicked()
                }
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                CardholderViewerState.Loading -> with(binding) {
                    cardViewerEditFab.isClickable = false
                }
                is CardholderViewerState.Success -> with(binding) {
                    cardViewerBarcodeImage.apply {
                        setBackgroundColor(state.cardColor)
                        loadBarcodeImage(state.barcodeFileName)
                    }
                    cardViewerCardNameText.text = state.cardName
                    cardViewerCardContentText.text = state.cardContent
                    cardViewerEditFab.isClickable = true
                }
            }
        }
    }
}
