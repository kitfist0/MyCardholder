package my.cardholder.ui.card.crop

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
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

    private val _state = MutableStateFlow(
        CardCropState(
            selectedImageUri = CardCropFragmentArgs.fromSavedStateHandle(savedStateHandle).imageUri,
            cropButtonClickEvent = false,
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
                    is ScanResult.Failure ->
                        showSnack(Text.Simple(scanResult.throwable.toString()))
                    ScanResult.Nothing ->
                        showSnack(Text.Resource(R.string.snack_message_barcode_not_found))
                }
            }
            .launchIn(viewModelScope)
    }

    fun onCropCompleted(inputImage: InputImage?) {
        inputImage?.let { scanResultRepository.scan(it) }
    }

    fun onOkFabClicked() {
        _state.update { it.copy(cropButtonClickEvent = true) }
    }

    fun consumeCropButtonClickEvent() {
        _state.update { it.copy(cropButtonClickEvent = false) }
    }
}
