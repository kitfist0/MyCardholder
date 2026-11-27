package my.cardholder.ui.category.delete

import my.cardholder.R
import my.cardholder.util.Text

data class DeleteCategoryState(
    val categoryName: String,
) {
    companion object {
        fun DeleteCategoryState.getTitleText(): Text {
            return Text.ResourceAndParams(R.string.delete_category_dialog_title, listOf(categoryName))
        }
    }
}
