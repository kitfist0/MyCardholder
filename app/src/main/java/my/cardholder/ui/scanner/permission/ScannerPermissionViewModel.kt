package my.cardholder.ui.scanner.permission

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.cardholder.BuildConfig
import my.cardholder.R
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.PermissionHelper
import javax.inject.Inject

@HiltViewModel
class ScannerPermissionViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val permissionHelper: PermissionHelper,
) : BaseViewModel() {

    private companion object {
        const val CAMERA_PERMISSION = "android.permission.CAMERA"
        const val APPLICATION_DETAILS_ACTION = "android.settings.APPLICATION_DETAILS_SETTINGS"
    }

    private val _state = MutableStateFlow<ScannerPermissionState>(ScannerPermissionState.Loading)
    val state = _state.asStateFlow()

    fun onResume() {
        checkPermission(requestPermission = false)
    }

    fun onAddManuallyButtonClicked() {
        viewModelScope.launch {
            cardRepository.insertRandomCard()
            navigate(ScannerPermissionFragmentDirections.fromPermissionToCardholder())
        }
    }

    fun onGrantFabClicked() {
        checkPermission(requestPermission = true)
    }

    fun onPermissionRequestResult(
        permission: String,
        isGranted: Boolean,
        shouldShowRationale: Boolean,
    ) {
        if (!isGranted && permissionHelper.isNeverAskAgainChecked(permission, shouldShowRationale)) {
            startActivity(
                action = APPLICATION_DETAILS_ACTION,
                uriString = "package:${BuildConfig.APPLICATION_ID}",
            )
        }
    }

    private fun checkPermission(requestPermission: Boolean) {
        if (!permissionHelper.isPermissionGranted(CAMERA_PERMISSION)) {
            _state.value = ScannerPermissionState.PermissionRequired(
                requiredPermission = CAMERA_PERMISSION,
                rationaleTextStringRes = R.string.permission_rationale_message_text,
                requestPermission = requestPermission,
            )
        } else {
            navigate(ScannerPermissionFragmentDirections.fromPermissionToPreview())
        }
    }
}
