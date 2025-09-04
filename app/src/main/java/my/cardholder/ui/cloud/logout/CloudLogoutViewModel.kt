package my.cardholder.ui.cloud.logout

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
class CloudLogoutViewModel @Inject constructor(
    @Google private val googleCloudSignInAssistant: CloudSignInAssistant,
    @Yandex private val yandexCloudSignInAssistant: CloudSignInAssistant,
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private companion object {
        const val LOGOUT_DELAY_MILLIS = 500L
    }

    private val _state = MutableStateFlow<CloudLogoutState>(CloudLogoutState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val cloudProvider = settingsRepository.cloudProvider.first()
            setDefaultState(cloudProvider)
        }
    }

    fun onLogoutFabClicked() {
        _state.value = CloudLogoutState.Loading
        viewModelScope.launch {
            delay(LOGOUT_DELAY_MILLIS)
            val cloudProvider = settingsRepository.cloudProvider.first()
            when (cloudProvider) {
                CloudProvider.GOOGLE -> googleCloudSignInAssistant.signOut()
                CloudProvider.YANDEX -> yandexCloudSignInAssistant.signOut()
            }
                .onSuccess {
                    settingsRepository.setCloudSyncEnabled(false)
                    navigateUp()
                }
                .onFailure {
                    showToast(Text.Simple("ERROR: ${it.message}"))
                    setDefaultState(cloudProvider)
                }
        }
    }

    private fun setDefaultState(cloudProvider: CloudProvider) {
        val accountName = when (cloudProvider) {
            CloudProvider.GOOGLE -> googleCloudSignInAssistant.signedInAccountName
            CloudProvider.YANDEX -> yandexCloudSignInAssistant.signedInAccountName
        }
        _state.value = CloudLogoutState.Default(
            selectedCloudProvider = cloudProvider,
            accountNameText = Text.Simple(accountName.orEmpty()),
        )
    }
}
