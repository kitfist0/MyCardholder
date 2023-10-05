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
import my.cardholder.data.model.Category
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
    private var selectedCategory: Category? = null

    private val _state = MutableStateFlow<CardSearchState>(
        CardSearchState.Default(emptyList(), null)
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
                        val cards = selectedCategory
                            ?.let { category -> cardRepository.searchCardsBy(name, category.id) }
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
            selectedCategory = categoryRepository.getCategoryByName(categoryName)
            setDefaultState()
        }
    }

    fun onCardClicked(cardId: Long, extras: Navigator.Extras) {
        navigate(CardSearchFragmentDirections.fromCardSearchToCardDisplay(cardId), extras)
    }

    private suspend fun setDefaultState() {
        val names = categoryRepository.getCategoryNames()
        val items = if (names.isEmpty() || selectedCategory != null) {
            emptyList()
        } else {
            listOf(CardSearchCategoryItem.HeaderItem)
                .plus(names.map { CardSearchCategoryItem.DefaultItem(it) })
        }
        _state.value = CardSearchState.Default(
            categoryItems = items,
            selectedCategoryName = selectedCategory?.name,
        )
    }
}
