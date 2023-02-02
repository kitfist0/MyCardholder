package my.cardholder.ui.cardholder.cards

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import my.cardholder.data.CardRepository
import my.cardholder.data.SettingsDataStore
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardholderCardsViewModel @Inject constructor(
    cardRepository: CardRepository,
    settingsDataStore: SettingsDataStore,
): BaseViewModel() {

    private val _state = MutableStateFlow<CardholderCardsState>(CardholderCardsState.Empty())
    val state = _state.asStateFlow()

    init {
        settingsDataStore.multiColumnListEnabled
            .combine(cardRepository.cards) { isMultiColumn, cards ->
                _state.value = if (cards.isNotEmpty()) {
                    CardholderCardsState.Success(cards, if (isMultiColumn) 2 else 1)
                } else {
                    CardholderCardsState.Empty()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onCardClicked(cardId: Long, extras: Navigator.Extras) {
        navigate(CardholderCardsFragmentDirections.fromCardsToViewer(cardId), extras)
    }

    fun onSearchFabClicked(extras: Navigator.Extras) {
        navigate(CardholderCardsFragmentDirections.fromCardsToSearch(), extras)
    }
}
