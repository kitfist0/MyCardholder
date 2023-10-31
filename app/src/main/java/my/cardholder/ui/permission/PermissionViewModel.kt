package my.cardholder.ui.permission

import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import my.cardholder.BuildConfig
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.BarcodeAnalyzer
import my.cardholder.util.CameraPermissionHelper
import my.cardholder.util.ext.getContentString
import my.cardholder.util.ext.getSupportedFormat
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val barcodeAnalyzer: BarcodeAnalyzer,
    private val cameraPermissionHelper: CameraPermissionHelper,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private companion object {
        const val APPLICATION_DETAILS_ACTION = "android.settings.APPLICATION_DETAILS_SETTINGS"
    }

    private val _state = MutableStateFlow<PermissionState>(PermissionState.Loading)
    val state = _state.asStateFlow()

    init {
        barcodeAnalyzer.barcodeChannel.receiveAsFlow()
            .onEach { barcode ->
                barcode?.getSupportedFormat()?.let { supportedFormat ->
                    val cardId = cardRepository.insertNewCard(
                        content = barcode.getContentString(),
                        format = supportedFormat,
                    )
                    navigate(PermissionFragmentDirections.fromPermissionToCardDisplay(cardId))
                }
            }
            .launchIn(viewModelScope)
    }

    fun onResume() {
        checkCameraPermission(requestPermissionIfNotGranted = false)
    }

    fun onGrantFabClicked() {
        checkCameraPermission(requestPermissionIfNotGranted = true)
    }

    fun onScanBarcodeFileButtonClicked() {
        _state.update {
            PermissionState.PermissionRequired(
                launchBarcodeFileSelectionRequest = true,
                launchCameraPermissionRequest = false,
            )
        }
    }

    fun onBarcodeFileSelectionRequestResult(uri: Uri?) {
        uri?.let { barcodeAnalyzer.analyze(it) }
    }

    fun onBarcodeFileSelectionRequestLaunched() {
        setPermissionRequiredState()
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
        setPermissionRequiredState()
    }

    private fun checkCameraPermission(requestPermissionIfNotGranted: Boolean) {
        if (!cameraPermissionHelper.isPermissionGranted()) {
            _state.update {
                PermissionState.PermissionRequired(
                    launchBarcodeFileSelectionRequest = false,
                    launchCameraPermissionRequest = requestPermissionIfNotGranted
                )
            }
        } else {
            navigate(PermissionFragmentDirections.fromPermissionToCardScan())
        }
    }

    private fun setPermissionRequiredState() {
        _state.update {
            PermissionState.PermissionRequired(
                launchBarcodeFileSelectionRequest = false,
                launchCameraPermissionRequest = false,
            )
        }
    }
}
