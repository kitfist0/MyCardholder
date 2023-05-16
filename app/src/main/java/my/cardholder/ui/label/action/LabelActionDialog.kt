package my.cardholder.ui.label.action

import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogLabelActionBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.util.ext.assistedViewModels
import my.cardholder.util.ext.collectWhenStarted
import javax.inject.Inject

@AndroidEntryPoint
class LabelActionDialog : BaseDialogFragment<DialogLabelActionBinding>(
    DialogLabelActionBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: LabelActionViewModelFactory

    private val args: LabelActionDialogArgs by navArgs()

    override val viewModel: LabelActionViewModel by assistedViewModels {
        viewModelFactory.create(args.labelText)
    }

    override fun initViews() {
        with(binding) {
            labelActionDeleteButton.setOnClickListener {
                viewModel.onDeleteButtonClicked()
            }
            labelActionEditButton.setOnClickListener {
                viewModel.onEditButtonClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            binding.labelActionTitleText.text = state.title
        }
    }
}
