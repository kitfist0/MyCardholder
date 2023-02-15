package my.cardholder.ui.settings.main

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.FragmentSettingsMainBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class SettingsMainFragment : BaseFragment<FragmentSettingsMainBinding>(
    FragmentSettingsMainBinding::inflate
) {

    override val viewModel: SettingsMainViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            settingsColorThemeButton.setOnClickListener {
                viewModel.onColorThemeButtonClicked()
            }
            settingsCardListViewButton.setOnClickListener {
                viewModel.onCardListViewButtonClicked()
            }
            settingsCoffeeButton.setOnClickListener {
                viewModel.onCoffeeButtonClicked()
            }
            settingsSupportedFormatsButton.setOnClickListener {
                viewModel.onSupportedFormatsButtonClicked()
            }
            settingsAboutAppButton.setOnClickListener {
                viewModel.onAboutAppButtonClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.nightModeEnabled) { isEnabled ->
            setupColorThemeButtonState(isEnabled)
        }
        collectWhenStarted(viewModel.multiColumnListOfCards) { isMultiColumn ->
            setupCardListViewButtonState(isMultiColumn)
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

    private fun setupCardListViewButtonState(isMultiColumn: Boolean) {
        binding.settingsCardListViewButton.apply {
            icon = ContextCompat.getDrawable(
                context,
                if (isMultiColumn) {
                    R.drawable.ic_list_single_column
                } else {
                    R.drawable.ic_list_multi_column
                }
            )
            text = getString(
                if (isMultiColumn) {
                    R.string.settings_switch_to_single_column_cards
                } else {
                    R.string.settings_switch_to_multi_column_cards
                }
            )
        }
    }
}
