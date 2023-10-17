package my.cardholder.util

import android.content.Context
import android.content.res.Configuration
import javax.inject.Inject

class NightModeChecker @Inject constructor(private val context: Context) {
    val isEnabled: Boolean
        get() {
            val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        }
}
