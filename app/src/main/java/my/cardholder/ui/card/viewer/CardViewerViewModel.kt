package my.cardholder.ui.card.viewer

import android.view.MenuItem
import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.BarcodeImageRepository
import my.cardholder.data.Card
import my.cardholder.data.CardDao
import my.cardholder.ui.base.BaseViewModel

class CardViewerViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val barcodeImageRepository: BarcodeImageRepository,
    private val cardDao: CardDao,
) : BaseViewModel() {

    private val _card = liveData {
        emit(cardDao.getCard(cardId))
    }
    val card: Flow<Card> = _card.asFlow()

    val barcodeBitmap = _card
        .map { barcodeImageRepository.getBarcodeBitmap(it.id, it.text, it.format) }
        .asFlow()

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
            cardDao.deleteCard(cardId)
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
