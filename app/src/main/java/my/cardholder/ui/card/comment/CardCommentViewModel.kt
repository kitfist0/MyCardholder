package my.cardholder.ui.card.comment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.ui.card.content.CardContentState
import javax.inject.Inject

@HiltViewModel
class CardCommentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private val cardId = CardCommentFragmentArgs.fromSavedStateHandle(savedStateHandle).cardId

    private var commentText: String? = null

    private val _state = MutableStateFlow<CardContentState>(CardContentState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val cardContent = cardRepository.getCardAndCategory(cardId).first()?.card?.content
            _state.value = CardContentState.Success(cardContent.orEmpty())
        }
    }

    fun onBackPressed() {
        updateCardCommentAndNavigateUp()
    }

    fun onCardCommentTextChanged(changedCardComment: String?) {
        commentText = changedCardComment
    }

    fun onOkFabClicked() {
        updateCardCommentAndNavigateUp()
    }

    private fun updateCardCommentAndNavigateUp() {
        val cardComment = commentText?.trim()
        viewModelScope.launch {
            cardRepository.updateCardComment(cardId, cardComment)
            navigateUp()
        }
    }
}
