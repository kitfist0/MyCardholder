package my.cardholder.ui.card.search

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
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
    private var selectedCategoryId: Long? = null

    private val _state = MutableStateFlow<CardSearchState>(
        CardSearchState.SearchInAllCategories(emptyList())
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            setDefaultState()
            while (isActive) {
                delay(MIN_UPDATE_INTERVAL_MILLIS)
                newSearchRequestText?.let { name ->
                    newSearchRequestText = null
                    if (name.isBlank()) {
                        setDefaultState()
                    } else {
                        val cards = selectedCategoryId
                            ?.let { cardRepository.searchCardsBy(name, it) }
                            ?: cardRepository.searchCardsBy(name)
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

    fun onHeaderItemClicked() {
        navigate(CardSearchFragmentDirections.fromCardSearchToCategoryList())
    }

    fun onCategoryItemClicked(categoryName: String) {
        viewModelScope.launch {
            val categoryAndCards = categoryRepository.getCategoryAndCards(categoryName)
            selectedCategoryId = categoryAndCards?.category?.id
            categoryAndCards?.cards?.let { cards ->
                _state.value = CardSearchState.SearchInCategory(
                    categoryName = categoryName,
                    categoryCards = cards,
                )
            }
        }
    }

    fun onCardClicked(cardId: Long, extras: Navigator.Extras) {
        navigate(CardSearchFragmentDirections.fromCardSearchToCardDisplay(cardId), extras)
    }

    private suspend fun setDefaultState() {
        selectedCategoryId = null
        val categoryNames = categoryRepository.getCategoryNames()
        val categoryItems = if (categoryNames.isEmpty()) {
            listOf(CardSearchCategoryItem.HeaderItem.AddCategories)
        } else {
            listOf(CardSearchCategoryItem.HeaderItem.EditCategories)
                .plus(categoryNames.map { CardSearchCategoryItem.DefaultItem(it) })
        }
        _state.value = CardSearchState.SearchInAllCategories(categoryItems)
    }
}
