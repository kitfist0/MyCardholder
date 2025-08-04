package my.cardholder.ui.card.list

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.data.model.CardAndCategory
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardListViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    settingsRepository: SettingsRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow<CardListState>(CardListState.Empty())
    val state = _state.asStateFlow()

    init {
        cardRepository.cardsAndCategories
            .onEach { cardsAndCategories ->
                val isMultiColumn = settingsRepository.multiColumnListEnabled.first()
                _state.value = if (cardsAndCategories.isNotEmpty()) {
                    CardListState.Success(
                        cardsAndCategories = cardsAndCategories,
                        spanCount = if (isMultiColumn) 2 else 1,
                        scrollUpEvent = false,
                    )
                } else {
                    CardListState.Empty()
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateCardPositions(cardsAndCategories: List<CardAndCategory>) {
        viewModelScope.launch {
            val cards = cardsAndCategories.map { it.card }
            cardRepository.updateCardPositions(cards)
        }
    }

    fun consumeScrollUpEvent() {
        val currentState = _state.value
        if (currentState is CardListState.Success) {
            _state.value = currentState.copy(scrollUpEvent = false)
        }
    }

    fun onCardClicked(cardId: Long, extras: Navigator.Extras) {
        navigate(CardListFragmentDirections.fromCardListToCardDisplay(cardId), extras)
    }

    fun onImportCardsFabClicked() {
        navigate(CardListFragmentDirections.fromCardListToCardBackup())
    }

    fun onSearchFabClicked(extras: Navigator.Extras) {
        navigate(CardListFragmentDirections.fromCardListToCardSearch(), extras)
    }
}
