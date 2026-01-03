package my.cardholder.ui.cloud.logout

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import my.cardholder.R
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

    private val _state = MutableStateFlow<CloudLogoutState>(CloudLogoutState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val cloudProvider = settingsRepository.cloudProvider.first()
            val accountName = getAccountName(cloudProvider)
            setDefaultState(cloudProvider, accountName)
        }
    }

    fun onLogoutFabClicked() {
        viewModelScope.launch {
            val cloudProvider = settingsRepository.cloudProvider.first()
            val accountName = getAccountName(cloudProvider)
            navigate(CloudLogoutFragmentDirections.fromCloudLogoutToLogoutConfirmation(accountName))
        }
    }

    private fun setDefaultState(cloudProvider: CloudProvider, accountName: String) {
        _state.value = CloudLogoutState.Default(
            selectedCloudProvider = cloudProvider,
            accountNameText = Text.Simple(accountName),
            confirmationDialogText = Text.ResourceAndParams(
                R.string.cloud_logout_confirmation_title_text,
                listOf(accountName)
            ),
        )
    }

    private fun getAccountName(cloudProvider: CloudProvider): String {
        return when (cloudProvider) {
            CloudProvider.GOOGLE -> googleCloudSignInAssistant.signedInAccountName
            CloudProvider.YANDEX -> yandexCloudSignInAssistant.signedInAccountName
        }.orEmpty()
    }
}
