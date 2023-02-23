package my.cardholder.ui.settings.main

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.SettingsDataStore
import my.cardholder.ui.base.BaseViewModel
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class SettingsMainViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val settingsDataStore: SettingsDataStore,
) : BaseViewModel() {

    val nightModeEnabled = settingsDataStore.nightModeEnabled

    val multiColumnListOfCards = settingsDataStore.multiColumnListEnabled

    fun onColorThemeButtonClicked() {
        viewModelScope.launch {
            val isEnabled = nightModeEnabled.first()
            settingsDataStore.setNightModeEnabled(!isEnabled)
        }
    }

    fun onCardListViewButtonClicked() {
        viewModelScope.launch {
            val isEnabled = multiColumnListOfCards.first()
            settingsDataStore.setMultiColumnListEnabled(!isEnabled)
        }
    }

    fun onExportCardsButtonClicked() {
        viewModelScope.launch {
            cardRepository.exportCards()
        }
    }

    fun onImportCardsResult(inputStream: InputStream?) {
        inputStream?.let {
            viewModelScope.launch {
                cardRepository.importCards(inputStream)
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
