package my.cardholder.ui.card.list

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.SettingsRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardListViewModel @Inject constructor(
    cardRepository: CardRepository,
    settingsRepository: SettingsRepository,
): BaseViewModel() {

    private val _state = MutableStateFlow<CardListState>(CardListState.Empty())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            var prevNumOfPinnedCards = cardRepository.getNumberOfPinnedCards()
            combine(
                cardRepository.cardsAndCategories,
                settingsRepository.multiColumnListEnabled,
            ) { cardsAndCategories, isMultiColumn ->
                val numOfPinnedCards = cardRepository.getNumberOfPinnedCards()
                _state.value = if (cardsAndCategories.isNotEmpty()) {
                    CardListState.Success(
                        cardsAndCategories = cardsAndCategories,
                        spanCount = if (isMultiColumn) 2 else 1,
                        scrollUpEvent = numOfPinnedCards > prevNumOfPinnedCards,
                    )
                } else {
                    CardListState.Empty()
                }
                prevNumOfPinnedCards = numOfPinnedCards
            }.collect()
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

    fun onCardLongClicked(cardId: Long) {
        navigate(CardListFragmentDirections.fromCardListToCardAction(cardId))
    }

    fun onImportCardsFabClicked() {
        navigate(CardListFragmentDirections.fromCardListToCardBackup())
    }

    fun onSearchFabClicked(extras: Navigator.Extras) {
        navigate(CardListFragmentDirections.fromCardListToCardSearch(), extras)
    }
}
