package my.cardholder.util

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import javax.inject.Inject

class PermissionHelper @Inject constructor(
    private val context: Context,
    private val preferences: SharedPreferences,
) {
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun isNeverAskAgainChecked(permission: String, shouldShowRationale: Boolean): Boolean {
        if (wasShowRationaleStatusAlreadyTrue(permission) && !shouldShowRationale) {
            // User clicked deny permission for the first time. Now shouldShowRationale is true.
            // Then the user denied the permission again and checked Never Ask Again.
            // Now shouldShowRationale is false.
            return true
        }
        if (shouldShowRationale) {
            saveShowRationaleStatusWasAlreadyTrue(permission)
        }
        return false
    }

    private fun wasShowRationaleStatusAlreadyTrue(permission: String): Boolean {
        return preferences.getBoolean(permission, false)
    }

    private fun saveShowRationaleStatusWasAlreadyTrue(permission: String) {
        preferences.edit().putBoolean(permission, true).apply()
    }
}
