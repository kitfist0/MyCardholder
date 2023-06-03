package my.cardholder.ui.card.labels

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.LabelDao
import my.cardholder.data.LabelRefDao
import my.cardholder.data.model.LabelRef
import my.cardholder.ui.base.BaseViewModel

class CardLabelsViewModel @AssistedInject constructor(
    @Assisted("card_id") private val cardId: Long,
    cardRepository: CardRepository,
    labelDao: LabelDao,
    private val labelRefDao: LabelRefDao,
) : BaseViewModel() {

    private var cardName: String = ""

    private val _state = MutableStateFlow<CardLabelsState>(CardLabelsState.Empty(cardName))
    val state = _state.asStateFlow()

    init {
        cardRepository.getCardWithLabels(cardId)
            .combine(labelDao.getLabels()) { cardWithLabels, allLabels ->
                cardName = cardWithLabels?.card?.name.orEmpty()
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
            }
            .onEach { items ->
                _state.value = if (items.isNotEmpty()) {
                    CardLabelsState.Success(cardName, items)
                } else {
                    CardLabelsState.Empty(cardName)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onManageLabelsFabClicked() {
        navigate(CardLabelsFragmentDirections.fromCardLabelsToLabelList())
    }

    fun onCardLabelClicked(cardLabel: CardLabelsItemState) {
        viewModelScope.launch {
            val labelRef = LabelRef(cardId, cardLabel.labelId)
            if (cardLabel.isChecked) {
                labelRefDao.delete(labelRef)
            } else {
                labelRefDao.insert(labelRef)
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
