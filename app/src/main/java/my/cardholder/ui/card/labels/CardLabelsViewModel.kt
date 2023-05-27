package my.cardholder.ui.card.labels

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import my.cardholder.data.CardRepository
import my.cardholder.data.LabelDao
import my.cardholder.ui.base.BaseViewModel

class CardLabelsViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    cardRepository: CardRepository,
    labelDao: LabelDao,
) : BaseViewModel() {

    private val _state = MutableStateFlow<CardLabelsState>(CardLabelsState.Empty())
    val state = _state.asStateFlow()

    init {
        cardRepository.getCardWithLabels(cardId)
            .combine(labelDao.getLabels()) { cardWithLabels, allLabels ->
                val cardLabels = cardWithLabels?.labels
                    ?.map { it.id }
                    .orEmpty()
                allLabels.map { label ->
                    CardLabelsItemState(
                        labelId = label.id,
                        labelText = label.text,
                        isChecked = cardLabels.contains(label.id),
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
    }
}

@AssistedFactory
interface CardLabelsViewModelFactory {
    fun create(
        @Assisted("card_id") cardId: Long,
    ): CardLabelsViewModel
}
