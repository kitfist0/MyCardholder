package my.cardholder.ui.card.edit

import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.widget.AutoCompleteTextView
import androidx.core.graphics.toColorInt
import androidx.core.transition.doOnEnd
import androidx.core.transition.doOnStart
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.FragmentCardEditBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.*

@AndroidEntryPoint
class CardEditFragment : BaseFragment<FragmentCardEditBinding>(
    FragmentCardEditBinding::inflate
) {

    private val args: CardEditFragmentArgs by navArgs()

    override val viewModel: CardEditViewModel by viewModels()

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets(top = false)
            val uniqueNameSuffix = args.cardId
            cardEditBarcodeImage.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                updateVerticalPaddingAfterApplyingWindowInsets()
            }
            cardEditDeleteCardButton.setOnClickListener {
                viewModel.onDeleteCardButtonClicked()
            }
            cardEditCardNameInputLayout.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                editText?.doAfterTextChanged { viewModel.onCardNameChanged(it?.toString()) }
            }
            cardEditCardContentInputLayout.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                editText?.setOnClickListener {
                    val extras = listOf(cardEditCardContentInputLayout).toNavExtras()
                    viewModel.onCardContentClicked(extras)
                }
            }
            cardEditCardCategoryInputLayout.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                editText?.doAfterTextChanged { viewModel.onCardCategoryNameChanged(it?.toString()) }
            }
            cardEditCardCommentInputLayout.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                editText?.setOnClickListener {
                    val extras = listOf(cardEditCardCommentInputLayout).toNavExtras()
                    viewModel.onCardCommentClicked(extras)
                }
            }
            cardEditCardColorInputLayout.editText
                ?.doAfterTextChanged { viewModel.onCardColorChanged(it?.toString()) }
            cardEditCardLogoInputLayout.editText?.apply {
                setDrawableEndClickListener { viewModel.onCardLogoHelpIconClicked() }
                doAfterTextChanged { viewModel.onCardLogoChanged(it?.toString()) }
            }
            cardEditBarcodeFormatInputLayout.editText
                ?.doAfterTextChanged { viewModel.onCardFormatChanged(it?.toString()) }
            cardEditOkFab.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                setOnClickListener { viewModel.onOkFabClicked() }
            }
            val transitionSet = sharedElementEnterTransition as TransitionSet
            transitionSet.doOnStart {
                cardEditDeleteCardButton.isVisible = false
                cardEditCardColorInputLayout.isVisible = false
                cardEditCardLogoInputLayout.isVisible = false
                cardEditBarcodeFormatInputLayout.isVisible = false
            }
            transitionSet.doOnEnd {
                cardEditDeleteCardButton.animateVisibilityChange()
                cardEditCardColorInputLayout.animateVisibilityChange()
                cardEditCardLogoInputLayout.animateVisibilityChange()
                cardEditBarcodeFormatInputLayout.animateVisibilityChange()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CardEditState.Loading -> with(binding) {
                    cardEditCardNameInputLayout.isEnabled = false
                    cardEditCardContentInputLayout.isEnabled = false
                    cardEditCardCategoryInputLayout.isEnabled = false
                    cardEditCardCommentInputLayout.isEnabled = false
                    cardEditCardColorInputLayout.isEnabled = false
                    cardEditCardLogoInputLayout.isEnabled = false
                    cardEditBarcodeFormatInputLayout.isEnabled = false
                }
                is CardEditState.Success -> with(binding) {
                    cardEditBarcodeImage.apply {
                        setBackgroundColor(state.cardColor.toColorInt())
                        loadBarcodeImage(state.barcodeFile)
                    }
                    cardEditCardNameInputLayout.apply {
                        isEnabled = true
                        editText?.setTextAndSelectionIfRequired(state.cardName)
                    }
                    cardEditCardContentInputLayout.apply {
                        isEnabled = true
                        editText?.setTextAndSelectionIfRequired(state.cardContent)
                    }
                    cardEditCardCategoryInputLayout.isEnabled = true
                    (cardEditCardCategoryInputLayout.editText as? AutoCompleteTextView)?.apply {
                        setTextAndSelectionIfRequired(state.cardCategoryName)
                        adapter ?: setDefaultAdapter(state.cardCategoryNames)
                    }
                    cardEditCardCommentInputLayout.apply {
                        isEnabled = true
                        editText?.setTextAndSelectionIfRequired(state.cardComment)
                    }
                    cardEditCardColorInputLayout.isEnabled = true
                    (cardEditCardColorInputLayout.editText as? AutoCompleteTextView)?.apply {
                        setTextAndSelectionIfRequired(state.cardColor)
                        setStartColoredSquareIcon(state.cardColor)
                        adapter ?: setAdapter(CardEditColorAdapter(context, state.cardColors))
                    }
                    cardEditCardLogoInputLayout.apply {
                        isEnabled = true
                        helperText = if (state.cardLogo.isBlank()) {
                            getString(R.string.card_edit_logo_helper_text)
                        } else {
                            null
                        }
                        editText?.setStartIconFromUrl(
                            imageUrl = state.cardLogo,
                            errorDrawableRes = R.drawable.ic_broken_img,
                        )
                        editText?.setTextAndSelectionIfRequired(state.cardLogo)
                    }
                    cardEditBarcodeFormatInputLayout.isEnabled = true
                    (cardEditBarcodeFormatInputLayout.editText as? AutoCompleteTextView)?.apply {
                        setTextAndSelectionIfRequired(state.barcodeFormatName)
                        adapter ?: setDefaultAdapter(state.barcodeFormatNames)
                    }
                }
            }
        }
    }
}
