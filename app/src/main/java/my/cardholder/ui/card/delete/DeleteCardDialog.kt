package my.cardholder.ui.card.delete

import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogDeleteCardBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.ui.card.delete.DeleteCardState.Companion.getTitleText
import my.cardholder.util.ext.assistedViewModels
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.textToString
import javax.inject.Inject

@AndroidEntryPoint
class DeleteCardDialog : BaseDialogFragment<DialogDeleteCardBinding>(
    DialogDeleteCardBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: DeleteCardViewModelFactory

    private val args: DeleteCardDialogArgs by navArgs()

    override val viewModel: DeleteCardViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    override fun initViews() {
        dialog?.window?.setWindowAnimations(-1)
        binding.deleteCardConfirmationButton.setOnClickListener {
            viewModel.onDeleteConfirmationButtonClicked()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            binding.deleteCardTitleText.text = textToString(state.getTitleText())
        }
    }
}
