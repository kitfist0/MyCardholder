package my.cardholder.ui.card.scan

import androidx.camera.core.ImageProxy
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.ScanResultRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.ScanResult
import my.cardholder.data.model.SupportedFormat
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.CameraPermissionHelper
import javax.inject.Inject

@HiltViewModel
class CardScanViewModel @Inject constructor(
    cameraPermissionHelper: CameraPermissionHelper,
    private val cardRepository: CardRepository,
    private val scanResultRepository: ScanResultRepository,
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private companion object {
        const val EXPLANATION_DURATION_MILLIS = 5000L
        const val SCAN_RESULT_DEBOUNCE_TIME_MILLIS = 500L
    }

    private val _state = MutableStateFlow(
        CardScanState(
            explanationIsVisible = false,
            preliminaryScanResult = null,
            launchBarcodeFileSelectionRequest = false,
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val expIsRequired = settingsRepository.explanationAboutCardScanIsRequired.first()
            if (expIsRequired) {
                _state.update { it.copy(explanationIsVisible = true) }
                delay(EXPLANATION_DURATION_MILLIS)
                settingsRepository.disableExplanationAboutCardScan()
                _state.update { it.copy(explanationIsVisible = false) }
            }
        }

        scanResultRepository.cameraScanResult
            .distinctUntilChanged()
            .debounce(SCAN_RESULT_DEBOUNCE_TIME_MILLIS)
            .onEach { scanResult ->
                _state.update { it.copy(preliminaryScanResult = scanResult as? ScanResult.Success) }
            }
            .launchIn(viewModelScope)

        if (!cameraPermissionHelper.isPermissionGranted()) {
            navigate(CardScanFragmentDirections.fromCardScanToPermission())
        }
    }

    fun onNewCameraImage(image: ImageProxy) {
        scanResultRepository.scan(image)
    }

    fun onPreliminaryResultButtonClicked() {
        viewModelScope.launch {
            _state.value.preliminaryScanResult?.let { scanResult ->
                _state.update { it.copy(preliminaryScanResult = null) }
                val cardId = insertNewCard(scanResult.content, scanResult.format)
                navigate(CardScanFragmentDirections.fromCardScanToCardDisplay(cardId))
            }
        }
    }

    fun onBarcodeFileSelectionRequestResult(uri: String?) {
        uri?.let { navigate(CardScanFragmentDirections.fromCardScanToCardCrop(it)) }
    }

    fun onBarcodeFileSelectionRequestLaunched() {
        _state.update { it.copy(launchBarcodeFileSelectionRequest = false) }
    }

    fun onSelectFileFabClicked() {
        _state.update { it.copy(launchBarcodeFileSelectionRequest = true) }
    }

    fun onAddManuallyFabClicked() {
        viewModelScope.launch {
            val cardId = cardRepository.insertNewCard()
            navigate(CardScanFragmentDirections.fromCardScanToCardDisplay(cardId))
        }
    }

    private suspend fun insertNewCard(content: String, supportedFormat: SupportedFormat): Long {
        return cardRepository.insertNewCard(content = content, format = supportedFormat)
    }
}
