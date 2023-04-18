package my.cardholder.ui.search

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
class SearchViewModel @Inject constructor(
    private val cardRepository: CardRepository,
) : BaseViewModel() {

    private companion object {
        const val MIN_UPDATE_INTERVAL_MILLIS = 250L
    }

    private var newSearchRequestText: String? = null

    private val _state = MutableStateFlow<SearchState>(
        SearchState.Empty(R.string.search_blank_message_text)
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                delay(MIN_UPDATE_INTERVAL_MILLIS)
                newSearchRequestText?.let { name ->
                    newSearchRequestText = null
                    _state.value = if (name.isBlank()) {
                        SearchState.Empty(R.string.search_blank_message_text)
                    } else {
                        val cards = cardRepository.searchForCardsWithNamesLike(name)
                        if (cards.isNotEmpty()) {
                            SearchState.Success(cards)
                        } else {
                            SearchState.Empty(R.string.search_nothing_found_message_text)
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
        navigate(SearchFragmentDirections.fromSearchToViewer(cardId), extras)
    }
}
