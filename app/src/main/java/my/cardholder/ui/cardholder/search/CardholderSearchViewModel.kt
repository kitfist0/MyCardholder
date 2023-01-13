package my.cardholder.ui.cardholder.search

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
class CardholderSearchViewModel @Inject constructor(
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private companion object {
        const val MIN_UPDATE_INTERVAL_MILLIS = 250L
    }

    private var newSearchRequestText: String? = null

    private val _state = MutableStateFlow<CardholderSearchState>(
        CardholderSearchState.Empty(R.string.search_blank_message)
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(MIN_UPDATE_INTERVAL_MILLIS)
                newSearchRequestText?.let { name ->
                    newSearchRequestText = null
                    _state.value = if (name.isBlank()) {
                        CardholderSearchState.Empty(R.string.search_blank_message)
                    } else {
                        val cards = cardRepository.searchForCardsWithNamesLike(name)
                        if (cards.isNotEmpty()) {
                            CardholderSearchState.Success(cards)
                        } else {
                            CardholderSearchState.Empty(R.string.search_nothing_found_message)
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
        navigate(CardholderSearchFragmentDirections.fromSearchToViewer(cardId), extras)
    }
}
