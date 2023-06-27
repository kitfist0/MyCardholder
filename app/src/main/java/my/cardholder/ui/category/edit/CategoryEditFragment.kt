package my.cardholder.ui.category.edit

import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCategoryEditBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.assistedViewModels
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.setTextAndSelectionIfRequired
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets
import javax.inject.Inject

@AndroidEntryPoint
class CategoryEditFragment : BaseFragment<FragmentCategoryEditBinding>(
    FragmentCategoryEditBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: CategoryEditViewModelFactory

    private val args: CategoryEditFragmentArgs by navArgs()

    override val viewModel: CategoryEditViewModel by assistedViewModels {
        viewModelFactory.create(args.categoryId)
    }

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            categoryEditToolbar.setOnMenuItemClickListener { menuItem ->
                viewModel.onMenuItemClicked(menuItem.itemId)
            }
            categoryEditCategoryNameInputLayout.editText
                ?.doAfterTextChanged { viewModel.onCategoryNameChanged(it?.toString()) }
            categoryEditOkFab.setOnClickListener {
                viewModel.onOkFabClicked()
            }
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
                        setMenuVisibility(!state.isNewCategory)
                    }
                    binding.categoryEditCategoryNameInputLayout.isEnabled = true
                    binding.categoryEditCategoryNameInputLayout.editText
                        ?.setTextAndSelectionIfRequired(state.categoryName)
                }
            }
        }
    }
}
