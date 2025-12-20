package my.cardholder.ui.payment.error

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text
import javax.inject.Inject

@HiltViewModel
class PaymentErrorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel() {

    val state = MutableStateFlow(
        PaymentErrorState(
            errorMessageText = Text.Simple(
                text = PaymentErrorFragmentArgs.fromSavedStateHandle(savedStateHandle).errorText
            )
        )
    ).asStateFlow()

    fun onPaymentErrorFabClicked() {
        navigateUp()
    }
}
