package my.cardholder.ui.coffee

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import my.cardholder.data.CoffeeRepository
import my.cardholder.ui.base.BaseViewModel
import my.cardholder.util.Text
import my.cardholder.util.billing.GooglePlayBillingAssistant
import javax.inject.Inject

@HiltViewModel
class CoffeeViewModel @Inject constructor(
    private val coffeeRepository: CoffeeRepository,
    private val billingAssistant: GooglePlayBillingAssistant,
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

    fun onCoffeeClicked(coffeeId: String) {
        billingAssistant.getBillingFlowParams(coffeeId) { result ->
            result.onSuccess { showToast(Text.Simple(coffeeId)) }
                .onFailure { showSnack(Text.Simple(it.message ?: "Unknown error")) }
        }
    }
}
