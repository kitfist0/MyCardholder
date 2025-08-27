package my.cardholder.ui.settings.cloud

import androidx.activity.result.ActivityResult
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.CloudProvider
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.GmsAvailabilityChecker
import javax.inject.Inject

@HiltViewModel
class CloudSettingsViewModel @Inject constructor(
    private val gmsAvailabilityChecker: GmsAvailabilityChecker,
    private val settingsRepository: SettingsRepository,
): BaseViewModel() {

    private val _state = MutableStateFlow<CloudSettingsState>(CloudSettingsState.Loading)
    val state = _state.asStateFlow()

    init {
        settingsRepository.cloudSyncEnabled
            .onEach { isEnabled ->
                if (isEnabled) {
                    val cloudProvider = settingsRepository.cloudProvider.first()
                    _state.value = CloudSettingsState.Enabled(cloudProvider, "")
                } else {
                    _state.value = CloudSettingsState.Disabled(CloudProvider.entries.toList())
                }
            }
            .launchIn(viewModelScope)
    }

    fun onGoogleCloudSignInRequestResult(activityResult: ActivityResult) {
    }

    fun onGoogleCloudSignInRequestLaunched() {
    }

    fun onGoogleDriveButtonClicked() {
        viewModelScope.launch {
            settingsRepository.setCloudProvider(CloudProvider.GOOGLE)
        }
    }

    fun onYandexDiskRadioButtonChecked() {
        viewModelScope.launch {
            settingsRepository.setCloudProvider(CloudProvider.YANDEX)
        }
    }
}
