package my.cardholder.ui.card.edit

import android.transition.TransitionInflater
import android.transition.TransitionSet
import android.widget.AutoCompleteTextView
import androidx.core.graphics.toColorInt
import androidx.core.transition.doOnEnd
import androidx.core.transition.doOnStart
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardEditBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.*
import javax.inject.Inject

@AndroidEntryPoint
class CardEditFragment : BaseFragment<FragmentCardEditBinding>(
    FragmentCardEditBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: CardEditViewModelFactory

    private val args: CardEditFragmentArgs by navArgs()

    override val viewModel: CardEditViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            val uniqueNameSuffix = args.cardId
            cardEditBarcodeImage.setPadding(getStatusBarHeight())
            cardEditCardNameInputLayout.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                editText?.doAfterTextChanged { viewModel.onCardNameChanged(it?.toString()) }
            }
            cardEditCardContentInputLayout.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                editText?.doAfterTextChanged { viewModel.onCardContentChanged(it?.toString()) }
            }
            cardEditBarcodeFormatInputLayout.editText
                ?.doAfterTextChanged { viewModel.onCardFormatChanged(it?.toString()) }
            cardEditCardColorInputLayout.editText
                ?.doAfterTextChanged { viewModel.onCardColorChanged(it?.toString()) }
            cardEditOkFab.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                setOnClickListener { viewModel.onOkFabClicked() }
            }
            root.updateVerticalPaddingAfterApplyingWindowInsets(top = false)
            val transitionSet = sharedElementEnterTransition as TransitionSet
            transitionSet.doOnStart {
                cardEditBarcodeFormatInputLayout.isVisible = false
                cardEditCardColorInputLayout.isVisible = false
            }
            transitionSet.doOnEnd {
                cardEditBarcodeFormatInputLayout.animateVisibilityChange()
                cardEditCardColorInputLayout.animateVisibilityChange()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CardEditState.Loading -> with(binding) {
                    cardEditCardNameInputLayout.isEnabled = false
                    cardEditCardContentInputLayout.isEnabled = false
                    cardEditBarcodeFormatInputLayout.isEnabled = false
                    cardEditCardColorInputLayout.isEnabled = false
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
                    cardEditBarcodeFormatInputLayout.isEnabled = true
                    (cardEditBarcodeFormatInputLayout.editText as? AutoCompleteTextView)?.apply {
                        setTextAndSelectionIfRequired(state.barcodeFormatName)
                        adapter ?: setDefaultAdapter(state.barcodeFormatNames)
                    }
                    cardEditCardColorInputLayout.isEnabled = true
                    (cardEditCardColorInputLayout.editText as? AutoCompleteTextView)?.apply {
                        setTextAndSelectionIfRequired(state.cardColor)
                        adapter ?: setDefaultAdapter(state.cardColors)
                    }
                }
            }
        }
    }
}
