package my.cardholder.ui.settings

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import my.cardholder.BuildConfig
import my.cardholder.data.SettingsRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private companion object {
        const val WEB_PAGE = "https://github.com/kitfist0/MyCardholder"
    }

    private val _nightModeEnabled = MutableStateFlow(settingsRepository.isNightModeEnabled)
    val nightModeEnabled = _nightModeEnabled.asStateFlow()

    fun onColorThemeButtonClicked() {
        val isEnabled = settingsRepository.reverseDefaultNightMode()
        _nightModeEnabled.value = isEnabled
    }

    fun onSupportedFormatsClicked() {
        startActivity("android.intent.action.VIEW", WEB_PAGE)
    }

    fun onAboutAppButtonClicked() {
        showSnack("ver.${BuildConfig.VERSION_NAME}")
    }
}
