package my.cardholder.data

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val preferences: SharedPreferences,
) {

    private companion object {
        const val NIGHT_MODE_KEY = "night_mode"
        const val MULTI_COLUMN_LIST_KEY = "multi_column_list"
    }

    val isNightModeEnabled get() = preferences.getBoolean(NIGHT_MODE_KEY, false)

    private val _multiColumnListEnabled =
        MutableStateFlow(preferences.getBoolean(MULTI_COLUMN_LIST_KEY, false))
    val multiColumnListEnabled = _multiColumnListEnabled.asStateFlow()

    fun reverseDefaultNightMode(): Boolean {
        val b = !isNightModeEnabled
        preferences.edit().putBoolean(NIGHT_MODE_KEY, b).apply()
        setDefaultNightMode()
        return b
    }

    fun setDefaultNightMode() {
        AppCompatDelegate.setDefaultNightMode(
            if (isNightModeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    fun reverseMultiColumnListPref() {
        val b = !_multiColumnListEnabled.value
        preferences.edit().putBoolean(MULTI_COLUMN_LIST_KEY, b).apply()
        _multiColumnListEnabled.value = b
    }
}
