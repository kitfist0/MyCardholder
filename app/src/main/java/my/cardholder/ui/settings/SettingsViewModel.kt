package my.cardholder.ui.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.SettingsRepository
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.GmsAvailabilityChecker
import my.cardholder.util.Text
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    gmsAvailabilityChecker: GmsAvailabilityChecker,
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(
            nightModeEnabled = false,
            multiColumnListEnabled = false,
            cloudSyncAvailable = gmsAvailabilityChecker.isAvailable,
            cloudSyncCardText = Text.Simple(""),
            cloudSyncEnabled = false,
            launchCloudSignInRequest = null,
        )
    )
    val state = _state.asStateFlow()

    init {
        settingsRepository.cloudSyncEnabled
            .onEach { isEnabled ->
                val cardText = if (isEnabled) {
                    val cloudProvider = settingsRepository.cloudProvider.first().toString()
                    Text.ResourceAndParams(
                        R.string.settings_cloud_sync_switch_on_text,
                        listOf(cloudProvider)
                    )
                } else {
                    Text.Resource(R.string.settings_cloud_sync_switch_off_text)
                }
                _state.update {
                    it.copy(
                        cloudSyncCardText = cardText,
                        cloudSyncEnabled = isEnabled
                    )
                }
            }
            .launchIn(viewModelScope)

        settingsRepository.nightModeEnabled
            .onEach { isEnabled ->
                _state.update { it.copy(nightModeEnabled = isEnabled) }
            }
            .launchIn(viewModelScope)

        settingsRepository.multiColumnListEnabled
            .onEach { isEnabled ->
                _state.update { it.copy(multiColumnListEnabled = isEnabled) }
            }
            .launchIn(viewModelScope)
    }

    fun onCloudSyncCardClicked() {
        navigate(SettingsFragmentDirections.fromSettingsToCloudSettings())
    }

    fun onColorThemeButtonClicked() {
        viewModelScope.launch {
            val isEnabled = settingsRepository.nightModeEnabled.first()
            settingsRepository.setNightModeEnabled(!isEnabled)
        }
    }

    fun onCardListViewButtonClicked() {
        viewModelScope.launch {
            val isEnabled = settingsRepository.multiColumnListEnabled.first()
            settingsRepository.setMultiColumnListEnabled(!isEnabled)
        }
    }

    fun onManageCategoriesButtonClicked() {
        navigate(SettingsFragmentDirections.fromSettingsToCategoryList())
    }

    fun onImportExportCardsButtonClicked() {
        navigate(SettingsFragmentDirections.fromSettingsToCardBackup())
    }

    fun onCoffeeButtonClicked() {
        navigate(SettingsFragmentDirections.fromSettingsToCoffee())
    }

    fun onSupportedFormatsButtonClicked() {
        navigate(SettingsFragmentDirections.fromSettingsToSpecs())
    }

    fun onAboutAppButtonClicked() {
        navigate(SettingsFragmentDirections.fromSettingsToInfo())
    }

    private suspend fun setCloudSyncEnabled(isEnabled: Boolean) {
        settingsRepository.setCloudSyncEnabled(isEnabled)
        showToast(
            Text.Resource(
                if (isEnabled) {
                    R.string.settings_cloud_sync_activation_message
                } else {
                    R.string.settings_cloud_sync_deactivation_message
                }
            )
        )
    }
}
