package my.cardholder.ui.settings

import androidx.activity.result.ActivityResult
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.cloud.signin.CloudSignInAssistant
import my.cardholder.data.SettingsRepository
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val cloudSignInAssistant: CloudSignInAssistant,
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(
            nightModeEnabled = false,
            multiColumnListEnabled = false,
            cloudSyncEnabled = cloudSignInAssistant.alreadySignedIn,
            launchCloudSignInRequest = null,
        )
    )
    val state = _state.asStateFlow()

    init {
        settingsRepository.cloudSyncEnabled
            .onEach { isEnabled ->
                _state.value = _state.value.copy(cloudSyncEnabled = isEnabled)
            }
            .launchIn(viewModelScope)
        settingsRepository.nightModeEnabled
            .onEach { _state.value = _state.value.copy(nightModeEnabled = it) }
            .launchIn(viewModelScope)
        settingsRepository.multiColumnListEnabled
            .onEach { _state.value = _state.value.copy(multiColumnListEnabled = it) }
            .launchIn(viewModelScope)
    }

    fun onCloudSyncSwitchCheckedChanged(isChecked: Boolean) {
        if (isChecked && !cloudSignInAssistant.alreadySignedIn) {
            _state.update { it.copy(launchCloudSignInRequest = cloudSignInAssistant.signInIntent) }
        } else if (!isChecked && cloudSignInAssistant.alreadySignedIn) {
            viewModelScope.launch {
                cloudSignInAssistant.signOut()
                    .onSuccess { setCloudSyncEnabled(false) }
                    .onFailure {
                        setCloudSyncEnabled(true)
                        showSnack(Text.Simple("${it.message}"))
                    }
            }
        }
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

    fun onCloudSignInRequestLaunched() {
        _state.update { it.copy(launchCloudSignInRequest = null) }
    }

    fun onCloudSignInRequestResult(activityResult: ActivityResult) {
        viewModelScope.launch {
            if (activityResult.resultCode == -1 && cloudSignInAssistant.alreadySignedIn) {
                setCloudSyncEnabled(true)
            } else {
                setCloudSyncEnabled(false)
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
