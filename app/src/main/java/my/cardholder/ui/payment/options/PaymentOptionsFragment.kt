package my.cardholder.ui.payment.options

import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.FragmentPaymentOptionsBinding
import my.cardholder.ui.base.BaseFragment
import my.cardholder.util.ext.updateVerticalPaddingAfterApplyingWindowInsets
import kotlin.getValue

@AndroidEntryPoint
class PaymentOptionsFragment : BaseFragment<FragmentPaymentOptionsBinding>(
    FragmentPaymentOptionsBinding::inflate
) {

    override val viewModel: PaymentOptionsViewModel by viewModels()

    override fun initViews() {
        with(binding) {
            root.updateVerticalPaddingAfterApplyingWindowInsets()
        }
    }

    override fun collectData() {
    }
}
