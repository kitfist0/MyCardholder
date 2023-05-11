package my.cardholder.ui.labels

import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardLabelsBinding
import my.cardholder.databinding.FragmentLabelsBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.ui.card.adapter.LabelAdapter
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets

@AndroidEntryPoint
class LabelsFragment : BaseFragment<FragmentLabelsBinding>(
    FragmentLabelsBinding::inflate
) {

    override val viewModel: LabelsViewModel by viewModels()

    private val listAdapter by lazy(LazyThreadSafetyMode.NONE) {
        LabelAdapter(
            onItemClick = { label ->
                viewModel.onLabelClicked(label)
            }
        )
    }

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            labelsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
            }
            labelsAddLabelFab.setOnClickListener {
                viewModel.onAddLabelFabClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is LabelsState.Empty -> {
                    binding.labelsAddLabelFab.isVisible = false
                    binding.labelsEmptyListMessageText.setText(state.messageRes)
                    listAdapter.submitList(null)
                }

                is LabelsState.Success -> {
                    binding.labelsAddLabelFab.isVisible = true
                    binding.labelsEmptyListMessageText.text = null
                    listAdapter.submitList(state.labels)
                }
            }
        }
    }
}
