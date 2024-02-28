package my.cardholder.ui.card.list

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
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
        cardRepository.cardsAndCategories
            .onEach { cardsAndCategories ->
                val isMultiColumn = settingsRepository.multiColumnListEnabled.first()
                _state.value = if (cardsAndCategories.isNotEmpty()) {
                    CardListState.Success(cardsAndCategories, if (isMultiColumn) 2 else 1)
                } else {
                    CardListState.Empty()
                }
            }
            .launchIn(viewModelScope)
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
