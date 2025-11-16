package my.cardholder.ui.card.display

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardDisplayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    cardRepository: CardRepository,
    settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private companion object {
        const val EXPLANATION_DURATION_MILLIS = 3000L
    }

    private val cardId = CardDisplayFragmentArgs.fromSavedStateHandle(savedStateHandle).cardId

    private val _state = MutableStateFlow<CardDisplayState>(CardDisplayState.Loading)
    val state = _state.asStateFlow()

    init {
        cardRepository.getCardAndCategory(cardId)
            .filterNotNull()
            .onEach { cardAndCategory ->
                val card = cardAndCategory.card
                val expIsRequired = settingsRepository.explanationAboutBarcodeZoomIsRequired.first()
                _state.value = CardDisplayState.Success(
                    barcodeFile = card.barcodeFile,
                    cardCategory = cardAndCategory.category?.name.orEmpty(),
                    cardLogo = card.logo,
                    cardName = card.name,
                    cardContent = card.content,
                    cardComment = card.comment.orEmpty(),
                    cardColor = card.getColorInt(),
                    explanationIsVisible = expIsRequired,
                )
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            if (settingsRepository.explanationAboutBarcodeZoomIsRequired.first()) {
                delay(EXPLANATION_DURATION_MILLIS)
                settingsRepository.disableExplanationAboutBarcodeZoom()
                (_state.value as? CardDisplayState.Success)
                    ?.let { _state.value = it.copy(explanationIsVisible = false) }
            }
        }
    }

    fun onBarcodeImageClicked(extras: Navigator.Extras) {
        navigate(CardDisplayFragmentDirections.fromCardDisplayToCardZoom(cardId), extras)
    }

    fun onCardContentTextLongClicked(extras: Navigator.Extras): Boolean {
        navigate(CardDisplayFragmentDirections.fromCardDisplayToCardContent(cardId), extras)
        return true
    }

    fun onCardCommentTextLongClicked(extras: Navigator.Extras): Boolean {
        navigate(CardDisplayFragmentDirections.fromCardDisplayToCardComment(cardId), extras)
        return true
    }

    fun onEditFabClicked(extras: Navigator.Extras) {
        navigate(CardDisplayFragmentDirections.fromCardDisplayToCardEdit(cardId), extras)
    }
}
