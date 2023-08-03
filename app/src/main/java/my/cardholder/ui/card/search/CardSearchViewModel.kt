package my.cardholder.ui.card.search

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.cardholder.data.CardRepository
import my.cardholder.data.CategoryRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CardSearchViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val categoryRepository: CategoryRepository,
) : BaseViewModel() {

    private companion object {
        const val MIN_UPDATE_INTERVAL_MILLIS = 250L
    }

    private var newSearchRequestText: String? = null

    private val _state = MutableStateFlow<CardSearchState>(
        CardSearchState.Default(emptyList())
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            setDefaultState()
            while (true) {
                delay(MIN_UPDATE_INTERVAL_MILLIS)
                newSearchRequestText?.let { name ->
                    newSearchRequestText = null
                    if (name.isBlank()) {
                        setDefaultState()
                    } else {
                        val cards = cardRepository.searchForCardsWithNamesLike(name)
                        _state.value = if (cards.isNotEmpty()) {
                            CardSearchState.Success(cards)
                        } else {
                            CardSearchState.NothingFound
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

    private suspend fun setDefaultState() {
        val categoryNames = categoryRepository.getCategoryNames()
        _state.value = CardSearchState.Default(categoryNames)
    }
}
