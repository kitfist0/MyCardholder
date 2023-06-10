package my.cardholder.ui.coffee

import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import my.cardholder.data.source.local.CoffeeDao
import my.cardholder.data.source.remote.PlayBillingApi
import my.cardholder.data.model.Coffee
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CoffeeViewModel @Inject constructor(
    private val coffeeDao: CoffeeDao,
    private val playBillingApi: PlayBillingApi,
) : BaseViewModel() {

    private val _state = MutableStateFlow(
        CoffeeState(
            coffees = emptyList(),
            launchBillingFlowRequest = null,
        )
    )
    val state = _state.asStateFlow()

    init {
        coffeeDao.getCoffees()
            .onEach { coffees ->
                _state.update { it.copy(coffees = coffees) }
            }
            .launchIn(viewModelScope)
        viewModelScope.launch {
            if (coffeeDao.isEmpty()) {
                playBillingApi.productIds.onEach { productId ->
                    coffeeDao.insert(
                        Coffee(id = productId, isPurchased = false)
                    )
                }
            }
            playBillingApi.acknowledgePurchasedProducts()
            updatePurchaseStatusOfCoffees()
        }
    }

    fun onCoffeeClicked(productId: String) {
        viewModelScope.launch {
            playBillingApi.getBillingFlowParams(productId)
                .onSuccess { billingFlowParams ->
                    _state.update { it.copy(launchBillingFlowRequest = billingFlowParams) }
                }
                .onFailure { showSnack(it.message.orEmpty()) }
        }
    }

    fun onBillingFlowLaunched() {
        _state.update { it.copy(launchBillingFlowRequest = null) }
    }

    fun onBillingFlowResult(billingResult: BillingResult) {
        viewModelScope.launch {
            playBillingApi.onBillingFlowResult(billingResult)
                .onSuccess { updatePurchaseStatusOfCoffees() }
                .onFailure { showSnack(it.message.orEmpty()) }
        }
    }

    private fun updatePurchaseStatusOfCoffees() {
        viewModelScope.launch {
            playBillingApi.getIdsOfPurchasedProducts()
                .onSuccess { purchasedIds ->
                    purchasedIds.onEach { productId ->
                        coffeeDao.insert(
                            Coffee(id = productId, isPurchased = true)
                        )
                    }
                }
        }
    }
}
