package my.cardholder.ui.label.edit

import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentLabelEditBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.assistedViewModels
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.setTextAndSelectionIfRequired
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets
import javax.inject.Inject

@AndroidEntryPoint
class LabelEditFragment : BaseFragment<FragmentLabelEditBinding>(
    FragmentLabelEditBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: LabelEditViewModelFactory

    private val args: LabelEditFragmentArgs by navArgs()

    override val viewModel: LabelEditViewModel by assistedViewModels {
        viewModelFactory.create(args.labelId)
    }

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            labelEditLabelTextInputLayout.editText
                ?.doAfterTextChanged { viewModel.onLabelTextChanged(it?.toString()) }
            labelEditOkFab.setOnClickListener {
                viewModel.onOkFabClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            when (state) {
                is LabelEditState.Loading -> {
                    binding.labelEditLabelTextInputLayout.apply {
                        hint = ""
                        isEnabled = false
                    }
                }
                is LabelEditState.Success -> {
                    binding.labelEditLabelTextInputLayout.apply {
                        setHint(state.hintRes)
                        isEnabled = true
                    }
                    binding.labelEditLabelTextInputLayout.editText
                        ?.setTextAndSelectionIfRequired(state.labelText)
                }
            }
        }
    }
}
