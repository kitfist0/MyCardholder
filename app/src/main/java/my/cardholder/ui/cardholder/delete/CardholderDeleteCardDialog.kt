package my.cardholder.ui.cardholder.delete

import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.DialogCardholderDeleteCardBinding
import my.cardholder.ui.base.BaseBottomSheetDialog
import my.cardholder.util.ext.assistedViewModels
import javax.inject.Inject

@AndroidEntryPoint
class CardholderDeleteCardDialog : BaseBottomSheetDialog<DialogCardholderDeleteCardBinding>(
    DialogCardholderDeleteCardBinding::inflate
) {

    @Inject
    lateinit var viewModelFactory: CardholderDeleteCardViewModelFactory

    private val args: CardholderDeleteCardDialogArgs by navArgs()

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
