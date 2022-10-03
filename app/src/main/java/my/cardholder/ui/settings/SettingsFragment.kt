package my.cardholder.ui.settings

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentSettingsBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    override val viewModel: SettingsViewModel by viewModels()

    override fun initViews() {
    }

    override fun collectData() {
        viewModel.text.collectWhenStarted {
        }
    }
}
