package my.cardholder.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import my.cardholder.R
import javax.inject.Inject

class PermissionHelper @Inject constructor(
    private val context: Context,
    private val preferences: SharedPreferences,
) {
    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun onPermissionIsNotGranted(
        permission: String,
        shouldShowRationale: Boolean,
        openAppInfoIfNeverAskChecked: Boolean,
    ) {
        when {
            wasShowRationaleStatusAlreadyTrue(permission) && !shouldShowRationale && openAppInfoIfNeverAskChecked ->
                // User clicked deny permission for the first time. Now shouldShowRationale is true.
                // Then the user denied the permission again and checked Never Ask Again.
                // Now shouldShowRationale is false.
                openApplicationDetailsSettings()
            shouldShowRationale ->
                saveShowRationaleStatusWasAlreadyTrue(permission)
        }
    }

    private fun wasShowRationaleStatusAlreadyTrue(permission: String): Boolean {
        return preferences.getBoolean(permission, false)
    }

    private fun saveShowRationaleStatusWasAlreadyTrue(permission: String) {
        preferences.edit().putBoolean(permission, true).apply()
    }

    private fun openApplicationDetailsSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + context.packageName)
        )
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        Toast.makeText(context, R.string.permission_toast, Toast.LENGTH_LONG).show()
        context.startActivity(intent)
    }
}
