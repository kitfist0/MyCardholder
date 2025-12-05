package my.cardholder.ui.payment.options

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import my.cardholder.data.model.PaymentOption
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class PaymentOptionsViewModel @Inject constructor() : BaseViewModel() {

    private val _state = MutableStateFlow<PaymentOptionsState>(PaymentOptionsState.Loading)
    val state = _state.asStateFlow()

    fun onPaymentOptionClicked(paymentOption: PaymentOption) {
        _state.value = PaymentOptionsState.Selection(
            PaymentOption.entries.map {
                PaymentOptionState(
                    paymentOption = it,
                    isSelected = it == paymentOption,
                )
            }
        )
    }

    fun onSubscribeFabClicked() {
        _state.value = PaymentOptionsState.Loading
        viewModelScope.launch {
            delay(1000)
            navigate(
                if (Random.nextBoolean()) {
                    PaymentOptionsFragmentDirections.fromPaymentOptionsToPaymentError("Unknown error text")
                } else {
                    PaymentOptionsFragmentDirections.fromPaymentOptionsToPaymentDetails()
                }
            )
        }
    }
}
