package my.cardholder.ui.categories

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCategoriesBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class CategoriesFragment : BaseFragment<FragmentCategoriesBinding>(
    FragmentCategoriesBinding::inflate
) {

    override val viewModel: CategoriesViewModel by viewModels()

    private val listAdapter by lazy(LazyThreadSafetyMode.NONE) {
        CategoriesAdapter(
            onItemClicked = { category ->
                viewModel.onCategoryClicked(category)
            }
        )
    }

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            categoriesRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
            }
            categoriesAddCategoryFab.setOnClickListener {
                viewModel.onAddCategoryFabClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CategoriesState.Empty -> {
                    binding.categoriesEmptyMessageText.setText(state.messageRes)
                    listAdapter.submitList(null)
                }
                is CategoriesState.Success -> {
                    binding.categoriesEmptyMessageText.text = null
                    listAdapter.submitList(state.categories)
                }
            }
        }
    }
}
