package my.cardholder.ui.permission

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.cardholder.BuildConfig
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.CameraPermissionHelper
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val cameraPermissionHelper: CameraPermissionHelper,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private companion object {
        const val APPLICATION_DETAILS_ACTION = "android.settings.APPLICATION_DETAILS_SETTINGS"
    }

    private val _state = MutableStateFlow<PermissionState>(PermissionState.Loading)
    val state = _state.asStateFlow()

    fun onResume() {
        checkCameraPermission(requestPermissionIfNotGranted = false)
    }

    fun onAddManuallyButtonClicked() {
        viewModelScope.launch {
            cardRepository.insertNewCard()
            navigate(PermissionFragmentDirections.fromPermissionToCardList())
        }
    }

    fun onGrantFabClicked() {
        checkCameraPermission(requestPermissionIfNotGranted = true)
    }

    fun onCameraPermissionRequestResult(
        isGranted: Boolean,
        shouldShowRationale: Boolean,
    ) {
        if (!isGranted && cameraPermissionHelper.isNeverAskAgainChecked(shouldShowRationale)) {
            startActivity(
                action = APPLICATION_DETAILS_ACTION,
                uriString = "package:${BuildConfig.APPLICATION_ID}",
            )
        }
    }

    fun onCameraPermissionRequestLaunched() {
        _state.update {
            PermissionState.PermissionRequired(launchCameraPermissionRequest = false)
        }
    }

    private fun checkCameraPermission(requestPermissionIfNotGranted: Boolean) {
        if (!cameraPermissionHelper.isPermissionGranted()) {
            _state.update {
                PermissionState.PermissionRequired(
                    launchCameraPermissionRequest = requestPermissionIfNotGranted
                )
            }
        } else {
            navigate(PermissionFragmentDirections.fromPermissionToCardScan())
        }
    }
}
