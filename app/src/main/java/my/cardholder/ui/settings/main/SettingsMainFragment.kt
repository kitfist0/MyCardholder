package my.cardholder.ui.settings.main

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.FragmentSettingsMainBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class SettingsMainFragment : BaseFragment<FragmentSettingsMainBinding>(
    FragmentSettingsMainBinding::inflate
) {

    override val viewModel: SettingsMainViewModel by viewModels()

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
        collectWhenStarted(viewModel.nightModeEnabled) { isEnabled ->
            setupColorThemeButtonState(isEnabled)
        }
    }

    private fun setupColorThemeButtonState(isEnabled: Boolean) {
        binding.settingsColorThemeButton.apply {
            icon = ContextCompat.getDrawable(
                context,
                if (isEnabled) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
            )
            text = getString(
                if (isEnabled) R.string.settings_switch_to_light_mode else R.string.settings_switch_to_dark_mode
            )
        }
    }
}
