package my.cardholder.ui.card.list

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import my.cardholder.data.CardRepository
import my.cardholder.data.source.SettingsDataStore
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardListViewModel @Inject constructor(
    cardRepository: CardRepository,
    settingsDataStore: SettingsDataStore,
): BaseViewModel() {

    private val _state = MutableStateFlow<CardListState>(CardListState.Empty())
    val state = _state.asStateFlow()

    init {
        settingsDataStore.multiColumnListEnabled
            .combine(cardRepository.cards) { isMultiColumn, cards ->
                _state.value = if (cards.isNotEmpty()) {
                    CardListState.Success(cards, if (isMultiColumn) 2 else 1)
                } else {
                    CardListState.Empty()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onCardClicked(cardId: Long, extras: Navigator.Extras) {
        navigate(CardListFragmentDirections.fromCardListToCardDisplay(cardId), extras)
    }

    fun onSearchFabClicked(extras: Navigator.Extras) {
        navigate(CardListFragmentDirections.fromCardListToCardSearch(), extras)
    }
}
