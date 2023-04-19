package my.cardholder.ui.card.edit

import android.transition.TransitionInflater
import android.transition.TransitionSet
import androidx.core.transition.doOnEnd
import androidx.core.view.isInvisible
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardholderEditorBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.*
import javax.inject.Inject

@AndroidEntryPoint
class CardEditFragment : BaseFragment<FragmentCardholderEditorBinding>(
    FragmentCardholderEditorBinding::inflate
) {

    private companion object {
        const val ALPHA_ANIM_DURATION = 300L
    }

    @Inject
    lateinit var viewModelFactory: CardEditViewModelFactory

    private val args: CardEditFragmentArgs by navArgs()

    private val listAdapter by lazy {
        CardEditColorsAdapter { color ->
            viewModel.onColorItemClicked(color)
        }
    }

    override val viewModel: CardEditViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            val uniqueNameSuffix = args.cardId
            cardEditorCardNameInputLayout.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                editText?.doAfterTextChanged { viewModel.onCardNameChanged(it?.toString()) }
            }
            cardEditorCardContentInputLayout.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                editText?.doAfterTextChanged { viewModel.onCardContentChanged(it?.toString()) }
            }
            cardEditorBarcodeFormatInputLayout.apply {
                alpha = 0f
                editText?.doAfterTextChanged { viewModel.onCardFormatChanged(it?.toString()) }
            }
            cardEditorColorsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = listAdapter
                alpha = 0f
            }
            cardEditorOkFab.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                setOnClickListener { viewModel.onOkFabClicked() }
            }
            root.updateVerticalPaddingAfterApplyingWindowInsets(top = false)
            (sharedElementEnterTransition as TransitionSet).doOnEnd {
                cardEditorColorsRecyclerView.animate()
                    .setDuration(ALPHA_ANIM_DURATION)
                    .alpha(1f)
                    .start()
                cardEditorBarcodeFormatInputLayout.animate()
                    .setDuration(ALPHA_ANIM_DURATION)
                    .alpha(1f)
                    .start()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CardEditState.Loading -> with(binding) {
                    cardEditorCardNameInputLayout.isEnabled = false
                    cardEditorCardContentInputLayout.isEnabled = false
                    cardEditorBarcodeFormatInputLayout.isEnabled = false
                    cardEditorColorsRecyclerView.isInvisible = true
                }
                is CardEditState.Success -> with(binding) {
                    cardEditorBarcodeImage.apply {
                        setBackgroundColor(state.cardColor)
                        loadBarcodeImage(state.barcodeFile)
                    }
                    cardEditorCardNameInputLayout.apply {
                        isEnabled = true
                        editText?.setTextAndSelectionIfRequired(state.cardName)
                    }
                    cardEditorCardContentInputLayout.apply {
                        isEnabled = true
                        editText?.setTextAndSelectionIfRequired(state.cardContent)
                    }
                    cardEditorBarcodeFormatInputLayout.apply {
                        isEnabled = true
                        setAutocompleteTextIfRequired(
                            items = state.barcodeFormatNames,
                            item = state.barcodeFormatName,
                        )
                    }
                    cardEditorColorsRecyclerView.isInvisible = false
                    listAdapter.submitList(state.cardColors)
                }
            }
        }
    }
}