package my.cardholder.ui.category.list

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import my.cardholder.data.CategoryRepository
import my.cardholder.data.model.Category
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    categoryRepository: CategoryRepository,
) : BaseViewModel() {

    private val _state = MutableStateFlow<CategoryListState>(CategoryListState.Empty())
    val state = _state.asStateFlow()

    init {
        categoryRepository.categoriesAndCards
            .onEach { categoriesAndCards ->
                _state.value = if (categoriesAndCards.isNotEmpty()) {
                    val items = categoriesAndCards.map {
                        CategoryListItem(
                            categoryId = it.category.id,
                            categoryName = it.category.name,
                            numOfCards = it.cards.size,
                        )
                    }
                    CategoryListState.Success(items)
                } else {
                    CategoryListState.Empty()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onCategoryListItemClicked(categoryListItem: CategoryListItem, extras: Navigator.Extras) {
        navigate(CategoryListFragmentDirections.fromCategoryListToCategoryEdit(categoryListItem.categoryId), extras)
    }

    fun onAddCategoryFabClicked() {
        navigate(CategoryListFragmentDirections.fromCategoryListToCategoryEdit(Category.NEW_CATEGORY_ID))
    }
}
