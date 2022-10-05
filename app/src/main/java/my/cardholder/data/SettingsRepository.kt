package my.cardholder.data

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val preferences: SharedPreferences,
) {

    companion object {
        private const val NIGHT_MODE_KEY = "night_mode"
    }

    val isNightModeEnabled get() = preferences.getBoolean(NIGHT_MODE_KEY, false)

    fun reverseDefaultNightMode(): Boolean {
        val isEnabled = !isNightModeEnabled
        preferences.edit().putBoolean(NIGHT_MODE_KEY, isEnabled).apply()
        setDefaultNightMode()
        return isEnabled
    }

    private fun setDefaultNightMode() {
        AppCompatDelegate.setDefaultNightMode(
            if (isNightModeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
