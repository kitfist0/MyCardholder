package my.cardholder.ui.card.viewer

import android.view.MenuItem
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import my.cardholder.R
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

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.card_viewer_action_delete) {
            showSnack("Delete clicked")
            return true
        }
        return super.onMenuItemSelected(menuItem)
    }

    fun onEditFabClicked(extras: Navigator.Extras) {
        navigate(CardViewerFragmentDirections.fromCardViewerToCardEditor(cardId), extras)
    }
}
