package my.cardholder.ui.card.editor

import android.text.Editable
import android.transition.TransitionInflater
import androidx.navigation.fragment.navArgs
import coil.load
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.data.model.Card.Companion.getBarcodeFile
import my.cardholder.databinding.FragmentCardEditorBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.assistedViewModels
import my.cardholder.util.setupUniqueTransitionNamesAndReturnSharedElements
import javax.inject.Inject

@AndroidEntryPoint
class CardEditorFragment : BaseFragment<FragmentCardEditorBinding>(
    FragmentCardEditorBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: CardEditorViewModelFactory

    private val args: CardEditorFragmentArgs by navArgs()

    override val viewModel: CardEditorViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        binding.setupUniqueTransitionNamesAndReturnSharedElements(
            uniqueSuffix = args.cardId,
            R.id.card_editor_card_name_input_layout,
            R.id.card_editor_card_text_input_layout,
            R.id.card_editor_barcode_image,
            R.id.card_editor_ok_fab,
        )
        binding.cardEditorOkFab.setOnClickListener {
            viewModel.onOkFabClicked()
        }
    }

    override fun collectData() {
        viewModel.card.collectWhenStarted { card ->
            binding.cardEditorBarcodeImage.load(card.getBarcodeFile(requireContext()))
            binding.cardEditorNameEditText.text = Editable.Factory.getInstance().newEditable(card.name)
            binding.cardEditorCardTextEditText.text = Editable.Factory.getInstance().newEditable(card.text)
        }
    }
}
