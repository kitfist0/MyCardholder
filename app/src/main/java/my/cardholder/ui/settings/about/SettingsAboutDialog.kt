package my.cardholder.ui.settings.about

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogSettingsAboutBinding
import my.cardholder.ui.base.BaseDialogFragment

@AndroidEntryPoint
class SettingsAboutDialog : BaseDialogFragment<DialogSettingsAboutBinding>(
    DialogSettingsAboutBinding::inflate
) {

    override val viewModel: SettingsAboutViewModel by viewModels()

    override fun initViews() {
    }

    override fun collectData() {
    }
}
