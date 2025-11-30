package my.cardholder.ui.payment.details

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.cardholder.R
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text
import javax.inject.Inject

@HiltViewModel
class PaymentDetailsViewModel @Inject constructor() : BaseViewModel() {
    val state = MutableStateFlow(
        PaymentDetailsState(
            messageText = Text.Resource(R.string.payment_details_message_text)
        )
    ).asStateFlow()
}
