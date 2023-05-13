package my.cardholder.ui.label.action

import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogLabelActionBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.util.ext.assistedViewModels
import javax.inject.Inject

@AndroidEntryPoint
class LabelActionDialog : BaseDialogFragment<DialogLabelActionBinding>(
    DialogLabelActionBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: LabelActionViewModelFactory

    private val args: LabelActionDialogArgs by navArgs()

    override val viewModel: LabelActionViewModel by assistedViewModels {
        viewModelFactory.create(args.labelValue)
    }

    override fun initViews() {
    }

    override fun collectData() {
    }
}
