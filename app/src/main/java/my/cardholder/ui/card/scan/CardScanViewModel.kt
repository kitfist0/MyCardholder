package my.cardholder.ui.card.scan

import androidx.camera.core.ImageProxy
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.CardRepository
import my.cardholder.data.ScanResultRepository
import my.cardholder.data.model.ScanResult
import my.cardholder.data.model.SupportedFormat
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.CameraPermissionHelper
import my.cardholder.util.Text
import javax.inject.Inject

@HiltViewModel
class CardScanViewModel @Inject constructor(
    cameraPermissionHelper: CameraPermissionHelper,
    private val cardRepository: CardRepository,
    private val scanResultRepository: ScanResultRepository,
) : BaseViewModel() {

    private companion object {
        const val EXPLANATION_DURATION_MILLIS = 5000L
    }

    private var prevCardContent: String? = null
    private var prevSupportedFormat: SupportedFormat? = null

    private val _state = MutableStateFlow(
        CardScanState(
            withExplanation = true,
            launchBarcodeFileSelectionRequest = false,
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            delay(EXPLANATION_DURATION_MILLIS)
            _state.update { it.copy(withExplanation = false) }
        }

        scanResultRepository.cameraScanResult
            .onEach { scanResult ->
                if (scanResult is ScanResult.Success) {
                    insertNewCard(scanResult.content, scanResult.format)?.let { cardId ->
                        navigate(CardScanFragmentDirections.fromCardScanToCardDisplay(cardId))
                    }
                }
            }
            .launchIn(viewModelScope)

        scanResultRepository.fileScanResult
            .onEach { scanResult ->
                when (scanResult) {
                    is ScanResult.Success ->
                        insertNewCard(scanResult.content, scanResult.format)?.let { cardId ->
                            navigate(CardScanFragmentDirections.fromCardScanToCardDisplay(cardId))
                        }
                    is ScanResult.Failure ->
                        showSnack(Text.Simple(scanResult.throwable.toString()))
                    ScanResult.Nothing ->
                        showSnack(Text.Resource(R.string.snack_message_barcode_not_found))
                }
            }
            .launchIn(viewModelScope)

        if (!cameraPermissionHelper.isPermissionGranted()) {
            navigate(CardScanFragmentDirections.fromCardScanToPermission())
        }
    }

    fun onNewCameraImage(image: ImageProxy) {
        scanResultRepository.scan(image)
    }

    fun onBarcodeFileSelectionRequestResult(inputImage: InputImage?) {
        inputImage?.let { scanResultRepository.scan(it) }
    }

    fun onBarcodeFileSelectionRequestLaunched() {
        _state.update { it.copy(launchBarcodeFileSelectionRequest = false) }
    }

    fun onSelectFileFabClicked() {
        _state.update { it.copy(launchBarcodeFileSelectionRequest = true) }
    }

    private suspend fun insertNewCard(content: String, supportedFormat: SupportedFormat): Long? {
        if (content == prevCardContent && supportedFormat == prevSupportedFormat) {
            return null
        }
        prevCardContent = content
        prevSupportedFormat = supportedFormat
        return cardRepository.insertNewCard(content = content, format = supportedFormat)
    }
}
