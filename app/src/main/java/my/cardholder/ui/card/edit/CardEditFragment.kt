package my.cardholder.ui.card.edit

import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.widget.AutoCompleteTextView
import androidx.core.graphics.toColorInt
import androidx.core.transition.doOnEnd
import androidx.core.transition.doOnStart
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.data.model.LogoSuggestion
import my.cardholder.databinding.FragmentCardEditBinding
import my.cardholder.databinding.ItemLogoSuggestionBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.ui.card.CardSystemBarsAppearance
import my.cardholder.util.LogoLoader
import my.cardholder.util.ext.*

@AndroidEntryPoint
class CardEditFragment : BaseFragment<FragmentCardEditBinding>(
    FragmentCardEditBinding::inflate
) {

    private val args: CardEditFragmentArgs by navArgs()

    override val viewModel: CardEditViewModel by viewModels()

    // The card color lies under the status bar at the top of the screen
    private val systemBarsAppearance = CardSystemBarsAppearance(this)

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
                setEndIconOnClickListener { viewModel.onCardContentScanIconClicked() }
            }
            cardEditCardCategoryInputLayout.setupUniqueTransitionName(uniqueNameSuffix)
            // By position, as a category may be named like the item of no category
            cardEditCardCategoryAutoCompleteText.setOnItemClickListener { _, _, position, _ ->
                val state = viewModel.state.value as? CardEditState.Success
                    ?: return@setOnItemClickListener
                viewModel.onCardCategorySelected(state.cardCategoryNames.getOrNull(position - 1))
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
                cardEditCardColorInputLayout.isVisible = false
                cardEditCardLogoInputLayout.isVisible = false
                cardEditBarcodeFormatInputLayout.isVisible = false
            }
            transitionSet.doOnEnd {
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
                        systemBarsAppearance.setCardColor(state.cardColor.toColorInt())
                        loadBarcodeImage(state.barcodeFile)
                    }
                    cardEditCardNameInputLayout.apply {
                        isEnabled = true
                        error = if (state.cardNameIsEmpty) {
                            getString(R.string.card_edit_name_is_empty_error_message)
                        } else {
                            null
                        }
                        // An error enables the space under the field, and it stays after the error
                        isErrorEnabled = state.cardNameIsEmpty
                        editText?.setTextAndSelectionIfRequired(state.cardName)
                    }
                    showLogoSuggestions(state.logoSuggestions)
                    cardEditCardContentInputLayout.apply {
                        isEnabled = true
                        editText?.setTextAndSelectionIfRequired(state.cardContent)
                    }
                    cardEditCardCategoryInputLayout.isEnabled = true
                    cardEditCardCategoryAutoCompleteText.apply {
                        val noCategoryName = getString(R.string.card_edit_no_category)
                        setTextAndSelectionIfRequired(state.cardCategoryName ?: noCategoryName)
                        // The item of no category goes first
                        adapter ?: setDefaultAdapter(listOf(noCategoryName) + state.cardCategoryNames)
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

    private fun showLogoSuggestions(suggestions: List<LogoSuggestion>): Unit = with(binding) {
        cardEditLogoSuggestionsScroll.isVisible = suggestions.isNotEmpty()
        // Recreated only when they change, so that loaded icons don't blink while typing
        if (cardEditLogoSuggestionsGroup.tag == suggestions) {
            return
        }
        cardEditLogoSuggestionsGroup.tag = suggestions
        cardEditLogoSuggestionsGroup.removeAllViews()
        val inflater = LayoutInflater.from(context)
        suggestions.forEach { suggestion ->
            val chip = ItemLogoSuggestionBinding.inflate(inflater, cardEditLogoSuggestionsGroup, false).root
            chip.text = suggestion.name
            chip.setOnClickListener { viewModel.onLogoSuggestionClicked(suggestion) }
            chip.loadLogoIcon(suggestion.logo)
            cardEditLogoSuggestionsGroup.addView(chip)
        }
        cardEditLogoSuggestionsScroll.scrollTo(0, 0)
    }

    /** A suggestion without its icon still names the brand, so a failed icon is just left out */
    private fun Chip.loadLogoIcon(logo: String) {
        LogoLoader(context).load(
            imageUrl = logo,
            sizePx = dpToPx(24),
            paddingPx = dpToPx(1),
            cornerRadiusPx = dpToPx(4),
            onSuccess = { chipIcon = it },
            onError = {},
        )
    }
}
