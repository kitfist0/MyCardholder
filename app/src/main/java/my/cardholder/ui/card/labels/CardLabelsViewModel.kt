package my.cardholder.ui.card.labels

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.LabelDao
import my.cardholder.ui.base.BaseViewModel

class CardLabelsViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    private val cardRepository: CardRepository,
    labelDao: LabelDao,
) : BaseViewModel() {

    private val _state = MutableStateFlow<CardLabelsState>(CardLabelsState.Empty())
    val state = _state.asStateFlow()

    init {
        cardRepository.getCard(cardId)
            .combine(labelDao.getLabels()) { card, allLabels ->
                val cardLabels = card?.labels.orEmpty()
                allLabels.map { label ->
                    CardLabelsItemState(
                        labelValue = label.text,
                        isChecked = cardLabels.contains(label.text),
                    )
                }
            }.onEach { items ->
                _state.value = if (items.isNotEmpty()) {
                    CardLabelsState.Success(items)
                } else {
                    CardLabelsState.Empty()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onManageLabelsFabClicked() {
        navigate(CardLabelsFragmentDirections.fromCardLabelsToLabelList())
    }

    fun onCardLabelClicked(cardLabel: CardLabelsItemState) {
        viewModelScope.launch {
            if (cardLabel.isChecked) {
                cardRepository.removeLabelFromCard(cardId, cardLabel.labelValue)
            } else {
                cardRepository.addLabelToCard(cardId, cardLabel.labelValue)
            }
        }
    }
}

@AssistedFactory
interface CardLabelsViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardLabelsViewModel
}
