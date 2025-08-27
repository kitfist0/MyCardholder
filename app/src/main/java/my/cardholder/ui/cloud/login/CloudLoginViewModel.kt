package my.cardholder.ui.cloud.login

import androidx.activity.result.ActivityResult
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.CloudProvider
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.GmsAvailabilityChecker
import javax.inject.Inject

@HiltViewModel
class CloudLoginViewModel @Inject constructor(
    private val gmsAvailabilityChecker: GmsAvailabilityChecker,
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow<CloudLoginState>(CloudLoginState.Loading)
    val state = _state.asStateFlow()

    init {
        settingsRepository.cloudProvider
            .onEach { provider ->
                _state.value = CloudLoginState.Choice(selectedCloudProvider = provider)
            }
            .launchIn(viewModelScope)
    }

    fun onGoogleCloudSignInRequestResult(activityResult: ActivityResult) {
    }

    fun onCloudSignInRequestLaunched() {
    }

    fun onGoogleDriveButtonClicked() {
        viewModelScope.launch {
            settingsRepository.setCloudProvider(CloudProvider.GOOGLE)
        }
    }

    fun onYandexDiskButtonChecked() {
        viewModelScope.launch {
            settingsRepository.setCloudProvider(CloudProvider.YANDEX)
        }
    }
}
