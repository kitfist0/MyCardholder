package my.cardholder.ui.card.delete

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogDeleteCardBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.ui.card.delete.DeleteCardState.Companion.getTitleText
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.textToString

@AndroidEntryPoint
class DeleteCardDialog : BaseDialogFragment<DialogDeleteCardBinding>(
    DialogDeleteCardBinding::inflate
) {

    override val viewModel: DeleteCardViewModel by viewModels()

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
