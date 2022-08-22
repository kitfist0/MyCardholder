package my.cardholder.ui.card.editor

import android.text.Editable
import android.transition.TransitionInflater
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardEditorBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.assistedViewModels
import my.cardholder.util.setupTransitionNamesAndReturnSharedElements
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
        ViewCompat.setTransitionName(binding.cardEditorNameEditText, "trans_name_${args.cardId}")
        ViewCompat.setTransitionName(binding.cardEditorCardTextEditText, "trans_text_${args.cardId}")
        ViewCompat.setTransitionName(binding.cardEditorBarcodeImage, "trans_barcode_${args.cardId}")
        ViewCompat.setTransitionName(binding.cardEditorOkFab, "trans_fab_${args.cardId}")
        binding.cardEditorOkFab.setOnClickListener {
            viewModel.onOkFabClicked()
        }
    }

    override fun collectData() {
        viewModel.card.collectWhenStarted { card ->
            binding.cardEditorNameEditText.text = Editable.Factory.getInstance().newEditable(card.name)
            binding.cardEditorCardTextEditText.text = Editable.Factory.getInstance().newEditable(card.text)
        }
    }
}
