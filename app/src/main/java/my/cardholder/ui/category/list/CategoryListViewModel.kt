package my.cardholder.ui.category.list

import androidx.lifecycle.viewModelScope
import androidx.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import my.cardholder.data.model.CategoryAndCards
import my.cardholder.data.source.CategoryDao
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryListViewModel @Inject constructor(
    categoryDao: CategoryDao,
) : BaseViewModel() {

    private val _state = MutableStateFlow<CategoryListState>(CategoryListState.Empty())
    val state = _state.asStateFlow()

    init {
        categoryDao.getCategoriesAndCards()
            .onEach { categoriesAndCards ->
                _state.value = if (categoriesAndCards.isNotEmpty()) {
                    CategoryListState.Success(categoriesAndCards)
                } else {
                    CategoryListState.Empty()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onCategoryClicked(categoryAndCards: CategoryAndCards, extras: Navigator.Extras) {
        navigate(CategoryListFragmentDirections.fromCategoryListToCategoryEdit(categoryAndCards.category.id), extras)
    }

    fun onAddCategoryFabClicked() {
        navigate(CategoryListFragmentDirections.fromCategoryListToCategoryEdit())
    }
}
