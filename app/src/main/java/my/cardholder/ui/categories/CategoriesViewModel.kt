package my.cardholder.ui.categories

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import my.cardholder.data.model.Category
import my.cardholder.data.source.CategoryDao
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    categoryDao: CategoryDao,
) : BaseViewModel() {

    private val _state = MutableStateFlow<CategoriesState>(CategoriesState.Empty())
    val state = _state.asStateFlow()

    init {
        categoryDao.getCategories()
            .onEach { categories ->
                _state.value = if (categories.isNotEmpty()) {
                    CategoriesState.Success(categories)
                } else {
                    CategoriesState.Empty()
                }
            }
            .launchIn(viewModelScope)
    }

    fun onCategoryClicked(category: Category) {
        showSnack(category.name)
    }

    fun onAddCategoryFabClicked() {
    }
}
