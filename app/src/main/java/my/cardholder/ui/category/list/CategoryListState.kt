package my.cardholder.ui.category.list

import androidx.annotation.StringRes
import my.cardholder.R
import my.cardholder.data.model.Category

sealed class CategoryListState {
    data class Empty(
        @StringRes val messageRes: Int = R.string.category_list_empty_list_message
    ) : CategoryListState()

    data class Success(val categories: List<Category>) : CategoryListState()
}
