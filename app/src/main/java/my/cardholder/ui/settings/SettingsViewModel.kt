package my.cardholder.ui.settings

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
class SettingsViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val settingsDataStore: SettingsDataStore,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(
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

    fun onManageLabelsButtonClicked() {
        navigate(SettingsFragmentDirections.fromSettingsToLabelList())
    }

    fun onExportCardsButtonClicked() {
        viewModelScope.launch {
            if (cardRepository.cards.first().isNotEmpty()) {
                _state.update { it.copy(launchCardsExport = true) }
            } else {
                showSnack("No cards to export")
            }
        }
    }

    fun onExportCardsLaunched() {
        _state.update { it.copy(launchCardsExport = false) }
    }

    fun onExportCardsResult(outputStream: OutputStream?) {
        outputStream?.let {
            viewModelScope.launch {
                cardRepository.exportCards(it)
                    .onSuccess { showSnack("Export completed") }
                    .onFailure { showSnack(it.message.orEmpty()) }
            }
        }
    }

    fun onImportCardsButtonClicked() {
        _state.update { it.copy(launchCardsImport = true) }
    }

    fun onImportCardsLaunched() {
        _state.update { it.copy(launchCardsImport = false) }
    }

    fun onImportCardsResult(inputStream: InputStream?) {
        inputStream?.let {
            viewModelScope.launch {
                cardRepository.importCards(it)
                    .onSuccess { showSnack("Import completed") }
                    .onFailure { showSnack(it.message.orEmpty()) }
            }
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
