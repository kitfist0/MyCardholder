package my.cardholder.ui.card.scan

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.model.SupportedFormat
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.BarcodeAnalyzer
import my.cardholder.util.CameraPermissionHelper
import javax.inject.Inject

@HiltViewModel
class CardScanViewModel @Inject constructor(
    cameraPermissionHelper: CameraPermissionHelper,
    private val barcodeAnalyzer: BarcodeAnalyzer,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private companion object {
        const val EXPLANATION_DURATION_MILLIS = 5000L
    }

    private var prevCardContent: String? = null
    private var prevSupportedFormat: SupportedFormat? = null

    private val _state = MutableStateFlow(
        CardScanState(
            withExplanation = true,
            launchFileSelectionRequest = false,
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            delay(EXPLANATION_DURATION_MILLIS)
            _state.update { it.copy(withExplanation = false) }
        }
        barcodeAnalyzer.barcodeChannel.receiveAsFlow()
            .onEach { onBarcodeResult(it) }
            .launchIn(viewModelScope)

        if (!cameraPermissionHelper.isPermissionGranted()) {
            navigate(CardScanFragmentDirections.fromCardScanToPermission())
        }
    }

    fun onFileSelectionRequestResult(uri: Uri?) {
        uri?.let { barcodeAnalyzer.analyze(it) }
    }

    fun onFileSelectionRequestLaunched() {
        _state.update { it.copy(launchFileSelectionRequest = false) }
    }

    fun onSelectFileFabClicked() {
        _state.update { it.copy(launchFileSelectionRequest = true) }
    }

    private fun onBarcodeResult(barcode: Barcode?) {
        barcode?.getSupportedFormat()?.let { supportedFormat ->
            insertCardAndNavigateToEditor(
                content = barcode.displayValue.toString(),
                supportedFormat = supportedFormat,
            )
        }
    }

    private fun insertCardAndNavigateToEditor(content: String, supportedFormat: SupportedFormat) {
        if (content == prevCardContent && supportedFormat == prevSupportedFormat) {
            return
        }
        prevCardContent = content
        prevSupportedFormat = supportedFormat
        viewModelScope.launch {
            val cardId = cardRepository.insertNewCard(content = content, format = supportedFormat)
            navigate(CardScanFragmentDirections.fromCardScanToCardDisplay(cardId))
        }
    }

    private fun Barcode.getSupportedFormat(): SupportedFormat? {
        return when (format) {
            Barcode.FORMAT_AZTEC -> SupportedFormat.AZTEC
            Barcode.FORMAT_CODABAR -> SupportedFormat.CODABAR
            Barcode.FORMAT_CODE_39 -> SupportedFormat.CODE_39
            Barcode.FORMAT_CODE_93 -> SupportedFormat.CODE_93
            Barcode.FORMAT_CODE_128 -> SupportedFormat.CODE_128
            Barcode.FORMAT_DATA_MATRIX -> SupportedFormat.DATA_MATRIX
            Barcode.FORMAT_EAN_8 -> SupportedFormat.EAN_8
            Barcode.FORMAT_EAN_13 -> SupportedFormat.EAN_13
            Barcode.FORMAT_ITF -> SupportedFormat.ITF
            Barcode.FORMAT_PDF417 -> SupportedFormat.PDF_417
            Barcode.FORMAT_QR_CODE -> SupportedFormat.QR_CODE
            Barcode.FORMAT_UPC_A -> SupportedFormat.UPC_A
            Barcode.FORMAT_UPC_E -> SupportedFormat.UPC_E
            else -> null
        }
    }
}
