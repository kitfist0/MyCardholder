package my.cardholder.ui.info

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogSettingsAboutBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class InfoDialog : BaseDialogFragment<DialogSettingsAboutBinding>(
    DialogSettingsAboutBinding::inflate
) {

    override val viewModel: InfoViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            aboutPolicyButton.setOnClickListener {
                viewModel.onPrivacyPolicyButtonClicked()
            }
            aboutSourceCodeButton.setOnClickListener {
                viewModel.onSourceCodeButtonClicked()
            }
            aboutCopyrightButton.setOnClickListener {
                viewModel.onCopyrightButtonClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.dialogTitle) { title ->
            binding.aboutDevTitleText.text = title
        }
    }
}