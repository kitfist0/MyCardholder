package my.cardholder.ui.card.editor

import android.text.Editable
import android.transition.TransitionInflater
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.data.Card.Companion.barcodeTransitionId
import my.cardholder.data.Card.Companion.fabTransitionId
import my.cardholder.data.Card.Companion.nameTransitionId
import my.cardholder.data.Card.Companion.textTransitionId
import my.cardholder.databinding.FragmentCardEditorBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.assistedViewModels
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
        binding.cardEditorOkFab.setOnClickListener {
            viewModel.onOkFabClicked()
        }
    }

    override fun collectData() {
        viewModel.card.collectWhenStarted { card ->
            ViewCompat.setTransitionName(binding.cardEditorOkFab, card.fabTransitionId())
            ViewCompat.setTransitionName(binding.cardEditorNameEditText, card.nameTransitionId())
            ViewCompat.setTransitionName(binding.cardEditorCardTextEditText, card.textTransitionId())
            ViewCompat.setTransitionName(binding.cardEditorBarcodeImage, card.barcodeTransitionId())
            binding.cardEditorNameEditText.text = Editable.Factory.getInstance().newEditable(card.name)
            binding.cardEditorCardTextEditText.text = Editable.Factory.getInstance().newEditable(card.text)
        }
    }
}
