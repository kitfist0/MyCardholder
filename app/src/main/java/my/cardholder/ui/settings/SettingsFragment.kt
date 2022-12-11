package my.cardholder.ui.settings

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.FragmentSettingsBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.contextCompatDrawable

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    override val viewModel: SettingsViewModel by viewModels()

    override fun initViews() {
        binding.settingsColorThemeButton.setOnClickListener {
            viewModel.onColorThemeButtonClicked()
        }
        binding.settingsSupportedFormatsButton.setOnClickListener {
            viewModel.onSupportedFormatsClicked()
        }
        binding.settingsAboutAppButton.setOnClickListener {
            viewModel.onAboutAppButtonClicked()
        }
    }

    override fun collectData() {
        viewModel.nightModeEnabled.collectWhenStarted { isEnabled ->
            setupColorThemeButtonState(isEnabled)
        }
    }

    private fun setupColorThemeButtonState(isEnabled: Boolean) {
        binding.settingsColorThemeButton.apply {
            icon = contextCompatDrawable(
                if (isEnabled) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
            )
            text = getString(
                if (isEnabled) R.string.settings_switch_to_light_mode else R.string.settings_switch_to_dark_mode
            )
        }
    }
}
