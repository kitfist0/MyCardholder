package my.cardholder.ui.cloud.logout

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import my.cardholder.data.SettingsRepository
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text
import javax.inject.Inject

@HiltViewModel
class CloudLogoutViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow<CloudLogoutState>(CloudLogoutState.Loading)
    val state = _state.asStateFlow()

    init {
        settingsRepository.cloudProvider
            .onEach { provider ->
                _state.value = CloudLogoutState.Info(
                    selectedCloudProvider = provider,
                    accountNameText = Text.Simple(""),
                )
            }
            .launchIn(viewModelScope)
    }
}
