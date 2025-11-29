package my.cardholder.ui.payment.error

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.cardholder.R
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text
import javax.inject.Inject

@HiltViewModel
class PaymentErrorViewModel @Inject constructor() : BaseViewModel() {
    val state = MutableStateFlow(
        PaymentErrorState(
            errorMessageText = Text.Resource(R.string.payment_error_unknown_error_message_text)
        )
    ).asStateFlow()
}
