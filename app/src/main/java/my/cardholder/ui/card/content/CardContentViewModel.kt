package my.cardholder.ui.card.content

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text

class CardContentViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private var contentText: String? = null

    private val _state = MutableStateFlow<CardContentState>(CardContentState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val cardContent = cardRepository.getCardAndCategory(cardId).first()?.card?.content
            contentText = cardContent
            _state.value = CardContentState.Success(cardContent.orEmpty())
        }
    }

    fun onCardContentChanged(changedCardContent: String?) {
        contentText = changedCardContent
    }

    fun onOkFabClicked() {
        viewModelScope.launch {
            val cardContent = contentText?.trim()
            when {
                cardContent.isNullOrEmpty() ->
                    showSnack(
                        Text.Resource(R.string.card_content_empty_text_error_message)
                    )
                else ->
                    cardRepository.updateCardContent(cardId, cardContent)
                        .also { navigateUp() }
            }
        }
    }
}

@AssistedFactory
interface CardContentViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardContentViewModel
}
