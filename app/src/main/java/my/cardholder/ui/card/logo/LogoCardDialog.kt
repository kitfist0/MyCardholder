package my.cardholder.ui.card.logo

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogCardLogoBinding
import my.cardholder.ui.base.BaseDialogFragment

@AndroidEntryPoint
class LogoCardDialog : BaseDialogFragment<DialogCardLogoBinding>(
    DialogCardLogoBinding::inflate
) {

    override val viewModel: LogoCardViewModel by viewModels()

    override fun initViews() {
        binding.cardLogoOkButton.setOnClickListener {
            viewModel.onOkButtonClicked()
        }
    }

    override fun collectData() {
    }
}
