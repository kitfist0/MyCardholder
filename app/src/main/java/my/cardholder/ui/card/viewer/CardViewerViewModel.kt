package my.cardholder.ui.card.viewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.Card
import my.cardholder.data.CardDao
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardViewerViewModel @Inject constructor(
    cardDao: CardDao,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel() {

    private val cardId = CardViewerFragmentArgs.fromSavedStateHandle(savedStateHandle).cardId

    private val _card = liveData {
        emit(cardDao.getCard(cardId))
    }
    val card: Flow<Card> = _card.asFlow()

    fun onEditFabClicked(extras: Navigator.Extras) {
        navigate(CardViewerFragmentDirections.fromCardViewerToCardEditor(cardId), extras)
    }
}
