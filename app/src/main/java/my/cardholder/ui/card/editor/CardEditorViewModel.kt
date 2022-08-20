package my.cardholder.ui.card.editor

import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.Card
import my.cardholder.data.CardDao
import my.cardholder.ui.base.BaseViewModel

class CardEditorViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    cardDao: CardDao,
) : BaseViewModel() {

    private val _card = liveData {
        emit(cardDao.getCard(cardId))
    }
    val card: Flow<Card> = _card.asFlow()

    fun onOkFabClicked() {
        navigateBack()
    }
}

@AssistedFactory
interface CardEditorViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardEditorViewModel
}
