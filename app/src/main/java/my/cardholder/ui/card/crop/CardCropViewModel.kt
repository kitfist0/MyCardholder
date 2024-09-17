package my.cardholder.ui.card.crop

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import my.cardholder.R
import my.cardholder.data.CardRepository
import my.cardholder.data.ScanResultRepository
import my.cardholder.data.model.ScanResult
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text
import javax.inject.Inject

@HiltViewModel
class CardCropViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val scanResultRepository: ScanResultRepository,
) : BaseViewModel() {

    private val imageUri = CardCropFragmentArgs.fromSavedStateHandle(savedStateHandle).imageUri

    private val _state = MutableStateFlow<CardCropState>(
        CardCropState.Selection(
            selectedImageUri = imageUri,
            startProcessingEvent = false,
        )
    )
    val state = _state.asStateFlow()

    init {
        scanResultRepository.fileScanResult
            .onEach { scanResult ->
                when (scanResult) {
                    is ScanResult.Success -> {
                        val cardId = cardRepository.insertNewCard(
                            content = scanResult.content,
                            format = scanResult.format
                        )
                        navigate(CardCropFragmentDirections.fromCardCropToCardDisplay(cardId))
                    }
                    is ScanResult.Failure -> {
                        setSelectionState()
                        showSnack(Text.Simple(scanResult.throwable.toString()))
                    }
                    is ScanResult.Nothing -> {
                        setSelectionState()
                        showSnack(Text.Resource(R.string.snack_message_barcode_not_found))
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onProcessingCompleted(inputImage: InputImage?) {
        inputImage?.let { scanResultRepository.scan(it) }
    }

    fun onOkFabClicked() {
        setSelectionState(startProcessing = true)
    }

    fun onProcessingStarted() {
        _state.value = CardCropState.Processing
    }

    private fun setSelectionState(startProcessing: Boolean = false) {
        _state.value = CardCropState.Selection(
            selectedImageUri = imageUri,
            startProcessingEvent = startProcessing,
        )
    }
}
