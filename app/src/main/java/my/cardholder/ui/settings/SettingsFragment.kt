package my.cardholder.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.FragmentSettingsBinding
import my.cardholder.ui.base.BaseFragment

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    override val viewModel: SettingsViewModel by viewModels()

    override fun initViews() {
        binding.settingsColorThemeButton.setOnClickListener {
            viewModel.onColorThemeButtonClicked()
        }
    }

    override fun collectData() {
        viewModel.setDefaultNightMode.collectWhenStarted { mode ->
            AppCompatDelegate.setDefaultNightMode(mode)
            binding.settingsColorThemeButton.apply {
                if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
                    icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_light_mode)
                    text = getString(R.string.settings_switch_to_light_mode)
                } else {
                    icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_dark_mode)
                    text = getString(R.string.settings_switch_to_dark_mode)
                }
            }
        }
    }
}
