package my.cardholder.ui.category.list

import android.transition.TransitionInflater
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigator
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
            onItemClicked = { item, sharedElements ->
                val extras = FragmentNavigator.Extras.Builder()
                    .addSharedElements(sharedElements)
                    .build()
                viewModel.onCategoryListItemClicked(item, extras)
            }
        )
    }

    override fun initViews() {
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            categoryListRecyclerView.apply {
                clipToPadding = false
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
                postponeEnterTransition()
                viewTreeObserver.addOnPreDrawListener {
                    startPostponedEnterTransition()
                    true
                }
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
                    listAdapter.submitList(state.categoriesAndCards)
                }
            }
        }
    }
}
