package my.cardholder.ui.payment.details

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentPaymentDetailsBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.collectWhenStarted
import my.cardholder.util.ext.textToString
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets
import kotlin.getValue

@AndroidEntryPoint
class PaymentDetailsFragment : BaseFragment<FragmentPaymentDetailsBinding>(
    FragmentPaymentDetailsBinding::inflate
) {

    override val viewModel: PaymentDetailsViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
            paymentDetailsOkFab.setOnClickListener {
                viewModel.onOkFabClicked()
            }
        }
    }

    override fun collectData() {
        collectWhenStarted(viewModel.state) { state ->
            binding.paymentDetailsMessageText.text = textToString(state.messageText)
        }
    }
}
