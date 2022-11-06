package my.cardholder.ui.cardholder.editor

import android.transition.TransitionInflater
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.navArgs
import coil.load
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.data.model.Card.Companion.getBarcodeFile
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.databinding.FragmentCardholderEditorBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.assistedViewModels
import my.cardholder.util.setupUniqueTransitionName
import javax.inject.Inject

@AndroidEntryPoint
class CardholderEditorFragment : BaseFragment<FragmentCardholderEditorBinding>(
    FragmentCardholderEditorBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: CardholderEditorViewModelFactory

    private val args: CardholderEditorFragmentArgs by navArgs()

    override val viewModel: CardholderEditorViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            val uniqueNameSuffix = args.cardId
            cardEditorBarcodeImage.setupUniqueTransitionName(uniqueNameSuffix)
            cardEditorCardNameInputLayout.setupUniqueTransitionName(uniqueNameSuffix)
            cardEditorCardTextInputLayout.setupUniqueTransitionName(uniqueNameSuffix)
            cardEditorOkFab.apply {
                setupUniqueTransitionName(uniqueNameSuffix)
                setOnClickListener { viewModel.onOkFabClicked() }
            }
            cardEditorCardNameEditText.doAfterTextChanged {
                viewModel.onCardNameChanged(it?.toString())
            }
            cardEditorCardTextEditText.doAfterTextChanged {
                viewModel.onCardTextChanged(it?.toString())
            }
            cardEditorColorPickerButton.setOnClickListener {
                viewModel.onColorPickerButtonClicked()
            }
        }
    }

    override fun collectData() {
        viewModel.card.collectWhenStarted { card ->
            with(binding) {
                cardEditorBackgroundColorView.setBackgroundColor(card.getColorInt())
                cardEditorBarcodeImage.load(card.getBarcodeFile(requireContext()))
                cardEditorCardNameEditText.setText(card.name)
                cardEditorCardTextEditText.setText(card.text)
            }
        }
    }
}