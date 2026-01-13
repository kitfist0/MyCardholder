package my.cardholder.ui.card.display

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.CardRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.Card.Companion.getColorInt
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text
import javax.inject.Inject

@HiltViewModel
class CardDisplayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    cardRepository: CardRepository,
    private val settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private companion object {
        const val ZOOM_EXPLANATION_ACTION_CODE = 1
    }

    private val cardId = CardDisplayFragmentArgs.fromSavedStateHandle(savedStateHandle).cardId

    private val _state = MutableStateFlow<CardDisplayState>(CardDisplayState.Loading)
    val state = _state.asStateFlow()

    init {
        cardRepository.getCardAndCategory(cardId)
            .filterNotNull()
            .onEach { cardAndCategory ->
                val card = cardAndCategory.card
                _state.value = CardDisplayState.Success(
                    barcodeFile = card.barcodeFile,
                    cardCategory = cardAndCategory.category?.name.orEmpty(),
                    cardLogo = card.logo,
                    cardName = card.name,
                    cardContent = card.content,
                    cardComment = card.comment.orEmpty(),
                    cardColor = card.getColorInt(),
                )
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            if (settingsRepository.explanationAboutBarcodeZoomIsRequired.first()) {
                showOkSnack(
                    ZOOM_EXPLANATION_ACTION_CODE,
                    Text.Resource(R.string.card_display_explanation_message)
                )
            }
        }
    }

    override fun onOkSnackButtonClicked(actionCode: Int) {
        viewModelScope.launch {
            if (actionCode == ZOOM_EXPLANATION_ACTION_CODE) {
                settingsRepository.disableExplanationAboutBarcodeZoom()
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
