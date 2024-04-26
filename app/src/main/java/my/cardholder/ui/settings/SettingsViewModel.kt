package my.cardholder.ui.settings

import androidx.activity.result.ActivityResult
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.cloud.CloudAssistant
import my.cardholder.data.SettingsRepository
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val cloudAssistant: CloudAssistant,
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(
            nightModeEnabled = false,
            multiColumnListEnabled = false,
            cloudSyncEnabled = cloudAssistant.isCloudAvailable,
            launchCloudSignInRequest = null,
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
        if (cloudAssistant.isCloudAvailable) {
            viewModelScope.launch {
                cloudAssistant.signOut()
                    .onSuccess { setCloudSyncEnabled(false) }
                    .onFailure { showSnack(Text.Simple("${it.message}")) }
            }
        } else {
            _state.update { it.copy(launchCloudSignInRequest = cloudAssistant.signInIntent) }
        }
    }

    fun onCloudSignInRequestLaunched() {
        _state.update { it.copy(launchCloudSignInRequest = null) }
    }

    fun onCloudSignInRequestResult(activityResult: ActivityResult) {
        if (activityResult.resultCode == -1 && cloudAssistant.isCloudAvailable) {
            setCloudSyncEnabled(true)
        } else {
            setCloudSyncEnabled(false)
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

    private fun setCloudSyncEnabled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCloudSyncEnabled(isEnabled)
            showSnack(
                Text.Simple(if (isEnabled) "Cloud sync enabled!" else "Cloud sync disabled!")
            )
        }
    }
}
