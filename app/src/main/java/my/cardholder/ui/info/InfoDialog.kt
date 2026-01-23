package my.cardholder.ui.info

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogInfoBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class InfoDialog : BaseDialogFragment<DialogInfoBinding>(
    DialogInfoBinding::inflate
) {

    override val viewModel: InfoViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            infoSupportedFormatsButton.setOnClickListener {
                viewModel.onSupportedFormatsButtonClicked()
            }
            infoPolicyButton.setOnClickListener {
                viewModel.onPrivacyPolicyButtonClicked()
            }
            infoSourceCodeButton.setOnClickListener {
                viewModel.onSourceCodeButtonClicked()
            }
            infoCopyrightButton.setOnClickListener {
                viewModel.onCopyrightButtonClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.dialogTitle) { title ->
            binding.infoDevTitleText.text = title
        }
    }
}
