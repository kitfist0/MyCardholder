package my.cardholder.ui.settings

import androidx.activity.result.contract.ActivityResultContracts
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

    private companion object {
        const val EXPORTED_FILE_NAME = "exported.csv"
        const val MIME_TYPE = "*/*"
    }

    private val exportCards =
        registerForActivityResult(ActivityResultContracts.CreateDocument(MIME_TYPE)) { uri ->
            val outputStream = uri?.let { requireActivity().contentResolver.openOutputStream(it) }
            viewModel.onExportCardsResult(outputStream)
        }

    private val importCards =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val inputStream = uri?.let { requireActivity().contentResolver.openInputStream(it) }
            viewModel.onImportCardsResult(inputStream)
        }

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
            settingsManageLabelsButton.setOnClickListener {
                viewModel.onManageLabelsButtonClicked()
            }
            settingsExportCardsButton.setOnClickListener {
                viewModel.onExportCardsButtonClicked()
            }
            settingsImportCardsButton.setOnClickListener {
                viewModel.onImportCardsButtonClicked()
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
            with(state) {
                setupColorThemeButtonState(nightModeEnabled)
                setupCardListViewButtonState(multiColumnListEnabled)
                if (launchCardsExport) {
                    exportCards.launch(EXPORTED_FILE_NAME)
                    viewModel.onExportCardsLaunched()
                }
                if (launchCardsImport) {
                    importCards.launch(MIME_TYPE)
                    viewModel.onImportCardsLaunched()
                }
            }
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
}
