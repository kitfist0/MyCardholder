package my.cardholder.ui.permission

import android.Manifest
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.PermissionHelper
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val permissionHelper: PermissionHelper,
) : BaseViewModel() {

    companion object {
        const val PERMISSION = Manifest.permission.CAMERA
    }

    private val _requestPermissions = MutableSharedFlow<Array<String>>()
    val requestPermissions = _requestPermissions.asSharedFlow()

    init {
        if (permissionHelper.isPermissionGranted(PERMISSION)) {
            navigate(PermissionFragmentDirections.fromPermissionToScanner())
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
        if (isGranted) {
            navigate(PermissionFragmentDirections.fromPermissionToScanner())
        } else {
            permissionHelper.onPermissionIsNotGranted(
                permission = permission,
                shouldShowRationale = shouldShowRationale,
                openAppInfoIfNeverAskChecked = true,
            )
        }
    }
}
