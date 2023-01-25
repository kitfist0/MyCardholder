package my.cardholder.ui.settings.main

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.cardholder.BuildConfig
import my.cardholder.data.SettingsRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsMainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private val _nightModeEnabled = MutableStateFlow(settingsRepository.isNightModeEnabled)
    val nightModeEnabled = _nightModeEnabled.asStateFlow()

    val multiColumnListOfCards = settingsRepository.multiColumnListEnabled

    fun onColorThemeButtonClicked() {
        val isEnabled = settingsRepository.reverseNightModePref()
        _nightModeEnabled.value = isEnabled
    }

    fun onCardListViewButtonClicked() {
        settingsRepository.reverseMultiColumnListPref()
    }

    fun onSupportedFormatsClicked() {
        navigate(SettingsMainFragmentDirections.fromSettingsToSpecs())
    }

    fun onAboutAppButtonClicked() {
        showSnack("ver.${BuildConfig.VERSION_NAME}")
    }
}
