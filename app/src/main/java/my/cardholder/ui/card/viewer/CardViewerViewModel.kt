package my.cardholder.ui.card.viewer

import android.view.MenuItem
import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.model.Card
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel

class CardViewerViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private val _card = liveData {
        emit(cardRepository.getCard(cardId))
    }
    val card: Flow<Card> = _card.asFlow()

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.card_viewer_action_delete) {
            deleteCard()
            return true
        }
        return super.onMenuItemSelected(menuItem)
    }

    fun onEditFabClicked(extras: Navigator.Extras) {
        navigate(CardViewerFragmentDirections.fromCardViewerToCardEditor(cardId), extras)
    }

    private fun deleteCard() {
        viewModelScope.launch {
            cardRepository.deleteCard(cardId)
            navigateBack()
        }
    }
}

@AssistedFactory
interface CardViewerViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardViewerViewModel
}
