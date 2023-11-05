package my.cardholder.ui.permission

import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import my.cardholder.BuildConfig
import my.cardholder.R
import my.cardholder.data.CardRepository
import my.cardholder.data.ScanResultRepository
import my.cardholder.data.model.ScanResult
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.CameraPermissionHelper
import my.cardholder.util.Text
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val cameraPermissionHelper: CameraPermissionHelper,
    private val cardRepository: CardRepository,
    private val scanResultRepository: ScanResultRepository,
) : BaseViewModel() {

    private companion object {
        const val APPLICATION_DETAILS_ACTION = "android.settings.APPLICATION_DETAILS_SETTINGS"
    }

    private val _state = MutableStateFlow<PermissionState>(PermissionState.Loading)
    val state = _state.asStateFlow()

    init {
        scanResultRepository.fileScanResult
            .onEach { scanResult ->
                when (scanResult) {
                    is ScanResult.Success ->
                        cardRepository.insertNewCard(
                            content = scanResult.content,
                            format = scanResult.format,
                        ).also { cardId ->
                            navigate(PermissionFragmentDirections.fromPermissionToCardDisplay(cardId))
                        }
                    is ScanResult.Failure ->
                        showSnack(Text.Simple(scanResult.throwable.toString()))
                    ScanResult.Nothing ->
                        showSnack(Text.Resource(R.string.snack_message_barcode_not_found))
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

    fun onBarcodeFileSelectionRequestResult(inputImage: InputImage?) {
        inputImage?.let { scanResultRepository.scan(it) }
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
