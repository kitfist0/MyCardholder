package my.cardholder.ui.category.list

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCategoryListBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class CategoryListFragment : BaseFragment<FragmentCategoryListBinding>(
    FragmentCategoryListBinding::inflate
) {

    override val viewModel: CategoryListViewModel by viewModels()

    private val listAdapter by lazy(LazyThreadSafetyMode.NONE) {
        CategoryListAdapter(
            onItemClicked = { category ->
                viewModel.onCategoryClicked(category)
            }
        )
    }

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            categoryListRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
            }
            categoryListAddCategoryFab.setOnClickListener {
                viewModel.onAddCategoryFabClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CategoryListState.Empty -> {
                    binding.categoryListEmptyMessageText.setText(state.messageRes)
                    listAdapter.submitList(null)
                }
                is CategoryListState.Success -> {
                    binding.categoryListEmptyMessageText.text = null
                    listAdapter.submitList(state.categories)
                }
            }
        }
    }
}
