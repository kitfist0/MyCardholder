package my.cardholder.ui.settings

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.FragmentSettingsBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(
    FragmentSettingsBinding::inflate
) {

    override val viewModel: SettingsViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            settingsColorThemeButton.setOnClickListener {
                viewModel.onColorThemeButtonClicked()
            }
            settingsCardListViewButton.setOnClickListener {
                viewModel.onCardListViewButtonClicked()
            }
            settingsManageCategoriesButton.setOnClickListener {
                viewModel.onManageCategoriesButtonClicked()
            }
            settingsImportExportCardsButton.setOnClickListener {
                viewModel.onImportExportCardsButtonClicked()
            }
            settingsCloudSyncButton.setOnClickListener {
                viewModel.onCloudSyncButtonClicked()
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
        collectWhenStarted(viewModel.state) { state ->
            setupColorThemeButtonState(state.nightModeEnabled)
            setupCardListViewButtonState(state.multiColumnListEnabled)
            setupCloudSyncButtonState(state.cloudSyncEnabled)
        }
    }

    private fun setupColorThemeButtonState(isNightMode: Boolean) {
        binding.settingsColorThemeButton.apply {
            setIconResource(
                if (isNightMode) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
            )
            setText(
                if (isNightMode) {
                    R.string.settings_switch_to_light_mode_button_text
                } else {
                    R.string.settings_switch_to_dark_mode_button_text
                }
            )
        }
    }

    private fun setupCardListViewButtonState(isMultiColumn: Boolean) {
        binding.settingsCardListViewButton.apply {
            setIconResource(
                if (isMultiColumn) {
                    R.drawable.ic_list_single_column
                } else {
                    R.drawable.ic_list_multi_column
                }
            )
            setText(
                if (isMultiColumn) {
                    R.string.settings_switch_to_single_column_button_text
                } else {
                    R.string.settings_switch_to_multi_column_button_text
                }
            )
        }
    }

    private fun setupCloudSyncButtonState(cloudSyncEnabled: Boolean) {
        binding.settingsCloudSyncButton.apply {
            setIconResource(
                if (cloudSyncEnabled) R.drawable.ic_cloud_off else R.drawable.ic_cloud_on
            )
            setText(
                if (cloudSyncEnabled) {
                    R.string.settings_cloud_sync_deactivation_button_text
                } else {
                    R.string.settings_cloud_sync_activation_button_text
                }
            )
        }
    }
}
