package my.cardholder.ui.cloud.login

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import my.cardholder.cloud.CloudSignInAssistant
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.CloudProvider
import my.cardholder.di.Google
import my.cardholder.di.Yandex
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text
import javax.inject.Inject

@HiltViewModel
class CloudLoginViewModel @Inject constructor(
    @Google private val googleCloudSignInAssistant: CloudSignInAssistant,
    @Yandex private val yandexCloudSignInAssistant: CloudSignInAssistant,
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private companion object {
        const val LOGIN_DELAY_MILLIS = 500L
    }

    private val loginIntentChannel = Channel<Intent>()
    val loginIntents = loginIntentChannel.receiveAsFlow()

    private val _state = MutableStateFlow<CloudLoginState>(CloudLoginState.Loading)
    val state = _state.asStateFlow()

    init {
        settingsRepository.cloudProvider
            .onEach { provider ->
                _state.value = CloudLoginState.Selection(selectedCloudProvider = provider)
            }
            .launchIn(viewModelScope)
    }

    fun onLoginRequestResult(activityResult: ActivityResult) {
        viewModelScope.launch {
            val provider = settingsRepository.cloudProvider.first()
            when (provider) {
                CloudProvider.GOOGLE ->
                    googleCloudSignInAssistant.onSignInResult(activityResult)

                CloudProvider.YANDEX ->
                    yandexCloudSignInAssistant.onSignInResult(activityResult)
            }
                .onSuccess {
                    settingsRepository.setCloudSyncEnabled(true)
                    navigateUp()
                }
                .onFailure {
                    showToast(Text.Simple("ERROR: ${it.message}"))
                    _state.value = CloudLoginState.Selection(selectedCloudProvider = provider)
                }
        }
    }

    fun onGoogleCloudCardClicked() {
        viewModelScope.launch {
            settingsRepository.setCloudProvider(CloudProvider.GOOGLE)
        }
    }

    fun onYandexCloudCardClicked() {
        viewModelScope.launch {
            settingsRepository.setCloudProvider(CloudProvider.YANDEX)
        }
    }

    fun onLoginFabClicked() {
        _state.value = CloudLoginState.Loading
        viewModelScope.launch {
            delay(LOGIN_DELAY_MILLIS)
            when (settingsRepository.cloudProvider.first()) {
                CloudProvider.GOOGLE ->
                    loginIntentChannel.send(googleCloudSignInAssistant.signInIntent)

                CloudProvider.YANDEX ->
                    loginIntentChannel.send(yandexCloudSignInAssistant.signInIntent)
            }
        }
    }
}
