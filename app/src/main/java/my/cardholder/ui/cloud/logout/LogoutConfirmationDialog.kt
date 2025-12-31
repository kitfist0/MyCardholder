package my.cardholder.ui.cloud.logout

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogLogoutConfirmationBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.textToString

@AndroidEntryPoint
class LogoutConfirmationDialog : BaseDialogFragment<DialogLogoutConfirmationBinding>(
    DialogLogoutConfirmationBinding::inflate
) {

    override val viewModel: CloudLogoutViewModel by viewModels()

    override fun initViews() {
        dialog?.window?.setWindowAnimations(-1)
        binding.logoutConfirmationButton.setOnClickListener {
            viewModel.onLogoutConfirmButtonClicked()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            (state as? CloudLogoutState.Default)?.let {
                binding.logoutConfirmationTitleText.text = textToString(it.confirmationDialogText)
            }
        }
    }
}
