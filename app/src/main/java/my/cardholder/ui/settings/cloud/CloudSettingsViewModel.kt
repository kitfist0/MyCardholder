package my.cardholder.ui.settings.cloud

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CloudSettingsViewModel @Inject constructor(): BaseViewModel() {
    private val _state = MutableStateFlow(CloudSettingsState.Loading)
    val state = _state.asStateFlow()
}
