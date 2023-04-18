package my.cardholder.ui.delete

import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogCardholderDeleteCardBinding
import my.cardholder.ui.base.BaseDialogFragment
import my.cardholder.util.ext.assistedViewModels
import javax.inject.Inject

@AndroidEntryPoint
class DeleteDialog : BaseDialogFragment<DialogCardholderDeleteCardBinding>(
    DialogCardholderDeleteCardBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: DeleteViewModelFactory

    private val args: DeleteDialogArgs by navArgs()

    override val viewModel: CardholderDeleteCardViewModel by assistedViewModels {
        viewModelFactory.create(args.cardId)
    }

    override fun initViews() {
        binding.deleteCardDeleteTextButton.setOnClickListener {
            viewModel.onDeleteButtonClicked()
        }
    }

    override fun collectData() {
    }
}
