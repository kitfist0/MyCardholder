package my.cardholder.ui.settings

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: SharedPreferences,
) : BaseViewModel() {

    companion object {
        private const val DARK_MODE_KEY = "dark_mode"
    }

    private val isDarkModeEnabled get() = preferences.getBoolean(DARK_MODE_KEY, false)

    private val _setDefaultNightMode = MutableStateFlow(AppCompatDelegate.MODE_NIGHT_NO)
    val setDefaultNightMode = _setDefaultNightMode.asStateFlow()

    fun onColorThemeButtonClicked() {
        val newValue = isDarkModeEnabled.not()
        preferences.edit().putBoolean(DARK_MODE_KEY, newValue).apply()
        _setDefaultNightMode.value = if (newValue) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
    }
}
