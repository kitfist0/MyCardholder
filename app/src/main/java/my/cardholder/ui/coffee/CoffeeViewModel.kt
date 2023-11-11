package my.cardholder.ui.coffee

import android.app.Activity
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.cardholder.data.CoffeeRepository
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text
import my.cardholder.util.billing.BillingAssistant
import java.lang.ref.WeakReference
import javax.inject.Inject

@HiltViewModel
class CoffeeViewModel @Inject constructor(
    private val coffeeRepository: CoffeeRepository,
    private val billingAssistant: BillingAssistant,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        CoffeeState(coffees = emptyList())
    )
    val state = _state.asStateFlow()

    init {
        coffeeRepository.coffees
            .onStart { coffeeRepository.initialize() }
            .onEach { coffees ->
                _state.update { it.copy(coffees = coffees) }
            }
            .launchIn(viewModelScope)

        billingAssistant.purchasedProductIds
            .onEach { purchasedIds ->
                coffeeRepository.updatePurchaseStatusOfCoffees(purchasedIds)
            }
            .launchIn(viewModelScope)
    }

    fun onCoffeeClicked(activityReference: WeakReference<Activity>, coffeeId: String) {
        viewModelScope.launch {
            billingAssistant.purchaseProduct(activityReference, coffeeId)
                .onFailure { showSnack(Text.Simple(it.message.orEmpty())) }
        }
    }
}
