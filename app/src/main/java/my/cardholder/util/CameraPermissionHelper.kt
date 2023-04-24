package my.cardholder.util

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import javax.inject.Inject

class CameraPermissionHelper @Inject constructor(
    private val context: Context,
    private val preferences: SharedPreferences,
) {
    private companion object {
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }

    fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun isNeverAskAgainChecked(shouldShowRationale: Boolean): Boolean {
        if (wasShowRationaleStatusAlreadyTrue() && !shouldShowRationale) {
            // User clicked deny permission for the first time. Now shouldShowRationale is true.
            // Then the user denied the permission again and checked Never Ask Again.
            // Now shouldShowRationale is false.
            return true
        }
        if (shouldShowRationale) {
            saveShowRationaleStatusWasAlreadyTrue()
        }
        return false
    }

    private fun wasShowRationaleStatusAlreadyTrue(): Boolean {
        return preferences.getBoolean(CAMERA_PERMISSION, false)
    }

    private fun saveShowRationaleStatusWasAlreadyTrue() {
        preferences.edit().putBoolean(CAMERA_PERMISSION, true).apply()
    }
}
