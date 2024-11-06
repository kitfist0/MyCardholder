package my.cardholder.ui.card.zoom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardZoomViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private val cardId = CardZoomFragmentArgs.fromSavedStateHandle(savedStateHandle).cardId

    private val _state = MutableStateFlow<CardZoomState>(CardZoomState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            cardRepository.getCardAndCategory(cardId).first()?.card
                ?.let { card ->
                    _state.value = CardZoomState.Success(
                        barcodeFile = card.barcodeFile,
                        cardColor = card.getColorInt(),
                    )
                }
        }
    }
}
