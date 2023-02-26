package my.cardholder.ui.settings.main

import androidx.activity.result.contract.ActivityResultContracts
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
            icon = ContextCompat.getDrawable(
                context,
                if (isNightMode) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
            )
            text = getString(
                if (isNightMode) R.string.settings_switch_to_light_mode else R.string.settings_switch_to_dark_mode
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
