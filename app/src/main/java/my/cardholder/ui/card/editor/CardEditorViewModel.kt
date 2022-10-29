package my.cardholder.ui.card.editor

import android.view.MenuItem
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.model.Card
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel

class CardEditorViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private var updatedCardName: String? = null
    private var updatedCardText: String? = null

    val card: Flow<Card> = cardRepository.getCard(cardId)

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.card_editor_action_color_picker) {
            showSnack("Navigate to color picker")
            return true
        }
        return super.onMenuItemSelected(menuItem)
    }

    fun onOkFabClicked() {
        when {
            updatedCardName.isNullOrEmpty() -> showSnack("Empty card name!")
            updatedCardText.isNullOrEmpty() -> showSnack("Empty card text!")
            else -> viewModelScope.launch {
                cardRepository.updateCardNameAndText(cardId, updatedCardName, updatedCardText)
                navigateBack()
            }
        }
    }

    fun onCardNameChanged(cardName: String?) {
        updatedCardName = cardName
    }

    fun onCardTextChanged(cardText: String?) {
        updatedCardText = cardText
    }
}

@AssistedFactory
interface CardEditorViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardEditorViewModel
}
