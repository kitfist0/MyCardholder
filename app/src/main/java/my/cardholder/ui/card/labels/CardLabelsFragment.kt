package my.cardholder.ui.card.labels

import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentCardLabelsBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.assistedViewModels
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets
import javax.inject.Inject

@AndroidEntryPoint
class CardLabelsFragment : BaseFragment<FragmentCardLabelsBinding>(
    FragmentCardLabelsBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: CardLabelsViewModelFactory

    private val args: CardLabelsFragmentArgs by navArgs()

    override val viewModel: CardLabelsViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    private val listAdapter by lazy(LazyThreadSafetyMode.NONE) {
        CardLabelsAdapter(
            onItemClick = { cardLabel ->
                viewModel.onCardLabelClicked(cardLabel)
            }
        )
    }

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            cardLabelsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = listAdapter
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is CardLabelsState.Empty -> {
                    binding.cardLabelsEmptyListMessageText.setText(state.messageRes)
                    listAdapter.submitList(null)
                }
                is CardLabelsState.Success -> {
                    binding.cardLabelsEmptyListMessageText.text = null
                    listAdapter.submitList(state.cardLabels)
                }
            }
        }
    }
}
