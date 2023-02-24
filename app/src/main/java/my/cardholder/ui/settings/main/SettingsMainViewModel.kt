package my.cardholder.ui.settings.main

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.SettingsDataStore
import my.cardholder.ui.base.BaseViewModel
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsMainViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val settingsDataStore: SettingsDataStore,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        SettingsMainState(
            nightModeEnabled = false,
            multiColumnListEnabled = false,
            launchCardsExport = false,
            launchCardsImport = false,
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

    fun onExportCardsButtonClicked() {
        _state.value = _state.value.copy(launchCardsExport = true)
    }

    fun onExportCardsLaunched() {
        _state.value = _state.value.copy(launchCardsExport = false)
    }

    fun onExportCardsResult(outputStream: OutputStream?) {
        outputStream?.let {
            viewModelScope.launch {
                cardRepository.exportCards(it)
            }
        }
    }

    fun onImportCardsButtonClicked() {
        _state.value = _state.value.copy(launchCardsImport = true)
    }

    fun onImportCardsLaunched() {
        _state.value = _state.value.copy(launchCardsImport = false)
    }

    fun onImportCardsResult(inputStream: InputStream?) {
        inputStream?.let {
            viewModelScope.launch {
                cardRepository.importCards(it)
            }
        }
    }

    fun onCoffeeButtonClicked() {
        navigate(SettingsMainFragmentDirections.fromSettingsToCoffee())
    }

    fun onSupportedFormatsButtonClicked() {
        navigate(SettingsMainFragmentDirections.fromSettingsToSpecs())
    }

    fun onAboutAppButtonClicked() {
        navigate(SettingsMainFragmentDirections.fromSettingsToAbout())
    }
}
