package my.cardholder.ui.category.edit

import android.transition.TransitionInflater
import android.transition.TransitionSet
import androidx.core.transition.doOnEnd
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.FragmentCategoryEditBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.onImeActionDone
import my.cardholder.util.ext.setTextAndSelectionIfRequired
import my.cardholder.util.ext.setupUniqueTransitionName
import my.cardholder.util.ext.showSoftInput
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class CategoryEditFragment : BaseFragment<FragmentCategoryEditBinding>(
    FragmentCategoryEditBinding::inflate
) {

    private val args: CategoryEditFragmentArgs by navArgs()

    override val viewModel: CategoryEditViewModel by viewModels()

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            val uniqueNameSuffix = args.categoryId
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            categoryEditToolbar.setOnMenuItemClickListener { menuItem ->
                viewModel.onMenuItemClicked(menuItem.itemId)
            }
            categoryEditCategoryNameInputLayout.setupUniqueTransitionName(uniqueNameSuffix)
            categoryEditCategoryNameInputLayout.editText?.apply {
                doAfterTextChanged { viewModel.onCategoryNameChanged(it?.toString()) }
                onImeActionDone { viewModel.onOkFabClicked() }
            }
            categoryEditOkFab.setOnClickListener {
                viewModel.onOkFabClicked()
            }
        }
        (sharedElementEnterTransition as TransitionSet).doOnEnd {
            binding.categoryEditCategoryNameInputLayout.showSoftInput()
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CategoryEditState.Loading -> {
                    binding.categoryEditCategoryNameInputLayout.isEnabled = false
                }
                is CategoryEditState.Success -> {
                    binding.categoryEditToolbar.apply {
                        setTitle(state.titleRes)
                        menu.findItem(R.id.category_delete_menu_item).isVisible = !state.isNewCategory
                    }
                    binding.categoryEditCategoryNameInputLayout.isEnabled = true
                    binding.categoryEditCategoryNameInputLayout.editText
                        ?.setTextAndSelectionIfRequired(state.categoryName)
                }
            }
        }
    }
}
