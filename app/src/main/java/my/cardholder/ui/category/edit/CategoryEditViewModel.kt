package my.cardholder.ui.category.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.CategoryRepository
import my.cardholder.data.model.Category
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text
import javax.inject.Inject

@HiltViewModel
class CategoryEditViewModel @Inject constructor(
   savedStateHandle: SavedStateHandle,
    private val categoryRepository: CategoryRepository,
) : BaseViewModel() {

    private val categoryId = CategoryEditFragmentArgs.fromSavedStateHandle(savedStateHandle).categoryId

    private var enteredCategoryName: String? = null

    private val _state = MutableStateFlow<CategoryEditState>(CategoryEditState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val categoryName = categoryRepository.getCategoryNameById(categoryId).orEmpty()
            _state.value = CategoryEditState.Success(
                titleRes = if (categoryId != Category.NEW_CATEGORY_ID) {
                    R.string.category_edit_category_toolbar_title
                } else {
                    R.string.category_edit_new_category_toolbar_title
                },
                isNewCategory = categoryId == Category.NEW_CATEGORY_ID,
                categoryName = categoryName,
            )
        }
    }

    fun onMenuItemClicked(itemId: Int): Boolean {
        if (itemId == R.id.category_delete_menu_item) {
            viewModelScope.launch {
                categoryRepository.deleteCategoryById(categoryId)
                navigateUp()
            }
            return true
        }
        return false
    }

    fun onCategoryNameChanged(categoryName: String?) {
        enteredCategoryName = categoryName
    }

    fun onOkFabClicked() {
        viewModelScope.launch {
            val categoryName = enteredCategoryName?.trim()
            when {
                categoryName.isNullOrEmpty() ->
                    showSnack(
                        Text.Resource(R.string.category_edit_name_is_empty_error_message)
                    )
                categoryName.equals(Category.NULL_NAME, ignoreCase = true) ->
                    showSnack(
                        Text.Resource(R.string.category_edit_name_is_forbidden_error_message)
                    )
                categoryName.length > Category.MAX_NAME_LENGTH ->
                    showSnack(
                        Text.ResourceAndParams(
                            R.string.category_edit_name_is_long_error_message,
                            listOf(Category.MAX_NAME_LENGTH),
                        )
                    )
                else ->
                    categoryRepository.upsertCategoryIfCategoryNameIsNew(categoryId, categoryName)
                        .also { navigateUp() }
            }
        }
    }
}
