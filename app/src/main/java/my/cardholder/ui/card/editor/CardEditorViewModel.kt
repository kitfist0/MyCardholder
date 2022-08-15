package my.cardholder.ui.card.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.Card
import my.cardholder.data.CardDao
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    cardDao: CardDao,
) : BaseViewModel() {

    private val _card = liveData {
        val cardId = CardEditorFragmentArgs.fromSavedStateHandle(savedStateHandle).cardId
        emit(cardDao.getCard(cardId))
    }
    val card: Flow<Card> = _card.asFlow()

    fun onOkFabClicked() {
        navigateBack()
    }
}
