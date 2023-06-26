package my.cardholder.ui.categories

import androidx.annotation.StringRes
import my.cardholder.R
import my.cardholder.data.model.Category

sealed class CategoriesState {
    data class Empty(
        @StringRes val messageRes: Int = R.string.categories_empty_list_message
    ) : CategoriesState()

    data class Success(val categories: List<Category>) : CategoriesState()
}
