package my.cardholder.ui.card.search

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.CardRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardSearchViewModel @Inject constructor(
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private companion object {
        const val MIN_UPDATE_INTERVAL_MILLIS = 250L
    }

    private var newSearchRequestText: String? = null

    private val _state = MutableStateFlow<CardSearchState>(
        CardSearchState.Empty(R.string.card_search_blank_message)
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(MIN_UPDATE_INTERVAL_MILLIS)
                newSearchRequestText?.let { name ->
                    newSearchRequestText = null
                    _state.value = if (name.isBlank()) {
                        CardSearchState.Empty(R.string.card_search_blank_message)
                    } else {
                        val cards = cardRepository.searchForCardsWithNamesLike(name)
                        if (cards.isNotEmpty()) {
                            CardSearchState.Success(cards)
                        } else {
                            CardSearchState.Empty(R.string.card_search_nothing_found_message)
                        }
                    }
                }
            }
        }
    }

    fun onSearchTextChanged(cardName: String?) {
        newSearchRequestText = cardName
    }

    fun onCardClicked(cardId: Long, extras: Navigator.Extras) {
        navigate(CardSearchFragmentDirections.fromCardSearchToCardDisplay(cardId), extras)
    }
}
