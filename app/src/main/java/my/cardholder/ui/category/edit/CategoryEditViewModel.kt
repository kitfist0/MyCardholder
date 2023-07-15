package my.cardholder.ui.category.edit

import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import my.cardholder.R
import my.cardholder.data.model.Category
import my.cardholder.data.source.CategoryDao
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text

class CategoryEditViewModel @AssistedInject constructor(
    @Assisted("category_id") private val categoryId: Long,
    private val categoryDao: CategoryDao,
) : BaseViewModel() {

    private companion object {
        const val NEW_CATEGORY_ID = 0L
        const val MAX_CATEGORY_NAME_LENGTH = 30
    }

    private var enteredCategoryName: String? = null

    private val _state = MutableStateFlow<CategoryEditState>(CategoryEditState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val categoryName = categoryDao.getCategoryById(categoryId)?.name.orEmpty()
            _state.value = CategoryEditState.Success(
                titleRes = if (categoryId != NEW_CATEGORY_ID) {
                    R.string.category_edit_category_toolbar_title
                } else {
                    R.string.category_edit_new_category_toolbar_title
                },
                isNewCategory = categoryId == NEW_CATEGORY_ID,
                categoryName = categoryName,
            )
        }
    }

    fun onMenuItemClicked(itemId: Int): Boolean {
        if (itemId == R.id.category_delete_menu_item) {
            viewModelScope.launch {
                categoryDao.deleteCategoryById(categoryId)
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
                    showSnack(Text.Simple("Invalid input"))
                categoryName.length > MAX_CATEGORY_NAME_LENGTH ->
                    showSnack(Text.Simple("Too long name"))
                categoryDao.getCategoryByName(categoryName) != null ->
                    navigateUp()
                else -> {
                    categoryDao.upsert(Category(categoryId, categoryName))
                    navigateUp()
                }
            }
        }
    }
}

@AssistedFactory
interface CategoryEditViewModelFactory {
    fun create(
        @Assisted("category_id") categoryId: Long,
    ): CategoryEditViewModel
}
