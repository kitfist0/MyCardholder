package my.cardholder.ui.cloud.logout.confirmation

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogLogoutConfirmationBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.ui.cloud.logout.confirmation.LogoutConfirmationState.Companion.getTitleText
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.textToString

@AndroidEntryPoint
class LogoutConfirmationDialog : BaseDialogFragment<DialogLogoutConfirmationBinding>(
    DialogLogoutConfirmationBinding::inflate
) {

    override val viewModel: LogoutConfirmationViewModel by viewModels()

    override fun initViews() {
        dialog?.window?.setWindowAnimations(-1)
        binding.logoutConfirmationButton.setOnClickListener {
            viewModel.onLogoutButtonClicked()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            binding.logoutConfirmationTitleText.text = textToString(state.getTitleText())
        }
    }
}
