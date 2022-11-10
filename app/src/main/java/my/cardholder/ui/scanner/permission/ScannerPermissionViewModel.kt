package my.cardholder.ui.scanner.permission

import android.Manifest
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.cardholder.BuildConfig
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.PermissionHelper
import javax.inject.Inject

@HiltViewModel
class ScannerPermissionViewModel @Inject constructor(
    private val permissionHelper: PermissionHelper,
) : BaseViewModel() {

    private companion object {
        const val PERMISSION = Manifest.permission.CAMERA
    }

    private val _uiVisibilityState = MutableStateFlow(false)
    val uiVisibilityState = _uiVisibilityState.asStateFlow()

    private val _requestPermissions = MutableSharedFlow<Array<String>>()
    val requestPermissions = _requestPermissions.asSharedFlow()

    init {
        _uiVisibilityState.value = false
    }

    fun onResume() {
        if (permissionHelper.isPermissionGranted(PERMISSION)) {
            navigate(ScannerPermissionFragmentDirections.fromPermissionToPreview())
        } else {
            _uiVisibilityState.value = true
        }
    }

    fun onGrantFabClicked() {
        viewModelScope.launch {
            _requestPermissions.emit(arrayOf(PERMISSION))
        }
    }

    fun onPermissionRequestResult(
        permission: String,
        isGranted: Boolean,
        shouldShowRationale: Boolean,
    ) {
        if (!isGranted && permissionHelper.isNeverAskAgainChecked(permission, shouldShowRationale)) {
            startActivity(
                action = "android.settings.APPLICATION_DETAILS_SETTINGS",
                uriString = "package:${BuildConfig.APPLICATION_ID}",
            )
        }
    }
}
