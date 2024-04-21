package my.cardholder.ui.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.SettingsRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(
            nightModeEnabled = false,
            multiColumnListEnabled = false,
            cloudSyncEnabled = false,
        )
    )
    val state = _state.asStateFlow()

    init {
        settingsRepository.cloudSyncEnabled
            .onEach { _state.value = _state.value.copy(cloudSyncEnabled = it) }
            .launchIn(viewModelScope)
        settingsRepository.nightModeEnabled
            .onEach { _state.value = _state.value.copy(nightModeEnabled = it) }
            .launchIn(viewModelScope)
        settingsRepository.multiColumnListEnabled
            .onEach { _state.value = _state.value.copy(multiColumnListEnabled = it) }
            .launchIn(viewModelScope)
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

    fun onCloudSyncButtonClicked() {
        viewModelScope.launch {
            val isEnabled = settingsRepository.cloudSyncEnabled.first()
            settingsRepository.setCloudSyncEnabled(!isEnabled)
        }
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
}
