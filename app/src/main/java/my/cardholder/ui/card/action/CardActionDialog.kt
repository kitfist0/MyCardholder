package my.cardholder.ui.card.action

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogCardActionBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.ui.card.action.CardActionState.Companion.getPinUnpinButtonText
import my.cardholder.ui.card.action.CardActionState.Companion.getTitleText
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.textToString

@AndroidEntryPoint
class CardActionDialog : BaseDialogFragment<DialogCardActionBinding>(
    DialogCardActionBinding::inflate
) {

    override val viewModel: CardActionViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            cardActionPinUnpinButton.setOnClickListener {
                viewModel.onPinUnpinButtonClicked()
            }
            cardActionDeleteButton.setOnClickListener {
                viewModel.onDeleteButtonClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            binding.cardActionTitleText.text = textToString(state.getTitleText())
            binding.cardActionPinUnpinButton.text = textToString(state.getPinUnpinButtonText())
        }
    }
}
