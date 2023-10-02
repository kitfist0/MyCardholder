package my.cardholder.ui.category.edit

import androidx.annotation.StringRes

sealed class CategoryEditState {
    data object Loading : CategoryEditState()

    data class Success(
        @StringRes val titleRes: Int,
        val isNewCategory: Boolean,
        val categoryName: String,
    ) : CategoryEditState()
}
