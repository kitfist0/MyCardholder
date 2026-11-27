package my.cardholder.ui.category.delete

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogDeleteCategoryBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.ui.category.delete.DeleteCategoryState.Companion.getTitleText
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.textToString

@AndroidEntryPoint
class DeleteCategoryDialog : BaseDialogFragment<DialogDeleteCategoryBinding>(
    DialogDeleteCategoryBinding::inflate
) {

    override val viewModel: DeleteCategoryViewModel by viewModels()

    override fun initViews() {
        binding.deleteCategoryConfirmationButton.setOnClickListener {
            viewModel.onDeleteConfirmationButtonClicked()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            binding.deleteCategoryTitleText.text = textToString(state.getTitleText())
        }
    }
}
