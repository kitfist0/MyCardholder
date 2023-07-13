package my.cardholder.ui.settings

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.source.SettingsDataStore
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(
            nightModeEnabled = false,
            multiColumnListEnabled = false,
        )
    )
    val state = _state.asStateFlow()

    init {
        settingsDataStore.nightModeEnabled
            .onEach { _state.value = _state.value.copy(nightModeEnabled = it) }
            .launchIn(viewModelScope)
        settingsDataStore.multiColumnListEnabled
            .onEach { _state.value = _state.value.copy(multiColumnListEnabled = it) }
            .launchIn(viewModelScope)
    }

    fun onColorThemeButtonClicked() {
        viewModelScope.launch {
            val isEnabled = settingsDataStore.nightModeEnabled.first()
            settingsDataStore.setNightModeEnabled(!isEnabled)
        }
    }

    fun onCardListViewButtonClicked() {
        viewModelScope.launch {
            val isEnabled = settingsDataStore.multiColumnListEnabled.first()
            settingsDataStore.setMultiColumnListEnabled(!isEnabled)
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
}
