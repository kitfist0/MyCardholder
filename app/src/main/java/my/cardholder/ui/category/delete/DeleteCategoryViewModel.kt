package my.cardholder.ui.category.delete

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.data.CategoryRepository
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class DeleteCategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository,
) : BaseViewModel() {

    private val categoryId = DeleteCategoryDialogArgs.fromSavedStateHandle(savedStateHandle).categoryId

    private val _state = MutableStateFlow(
        DeleteCategoryState(categoryName = "")
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.getCategoryNameById(categoryId)?.let { categoryName ->
                _state.value = DeleteCategoryState(categoryName = categoryName)
            }
        }
    }

    fun onDeleteConfirmationButtonClicked() {
        viewModelScope.launch {
            categoryRepository.deleteCategoryById(categoryId)
            navigate(DeleteCategoryDialogDirections.fromDeleteCategoryToCategoryList())
        }
    }
}
