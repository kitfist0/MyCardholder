package my.cardholder.ui.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import my.cardholder.data.SettingsRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private val _defaultNightMode = MutableStateFlow(settingsRepository.defaultNightMode)
    val defaultNightMode = _defaultNightMode.asStateFlow()

    fun onColorThemeButtonClicked() {
        val mode = settingsRepository.reverseDefaultNightMode()
        _defaultNightMode.value = mode
    }
}
