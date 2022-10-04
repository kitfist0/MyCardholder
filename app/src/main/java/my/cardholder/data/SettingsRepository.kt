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

    val defaultNightMode = isDarkModeEnabled.toNightModeInt()

    fun reverseDefaultNightMode(): Int {
        val newValue = isDarkModeEnabled.not()
        preferences.edit().putBoolean(NIGHT_MODE_KEY, newValue).apply()
        return newValue.toNightModeInt()
    }

    private val isDarkModeEnabled get() = preferences.getBoolean(NIGHT_MODE_KEY, false)

    private fun Boolean.toNightModeInt() = if (this) {
        AppCompatDelegate.MODE_NIGHT_YES
    } else {
        AppCompatDelegate.MODE_NIGHT_NO
    }
}
