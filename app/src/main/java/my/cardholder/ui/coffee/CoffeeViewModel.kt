package my.cardholder.ui.coffee

import android.app.Activity
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import my.cardholder.data.CoffeeDao
import my.cardholder.data.PlayBillingClient
import my.cardholder.data.model.Coffee
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class CoffeeViewModel @Inject constructor(
    private val coffeeDao: CoffeeDao,
    private val playBillingClient: PlayBillingClient,
) : BaseViewModel() {

    val coffees = coffeeDao.getCoffees()

    init {
        viewModelScope.launch {
            if (coffeeDao.isEmpty()) {
                playBillingClient.productIds.onEach { productId ->
                    coffeeDao.insert(
                        Coffee(id = productId, isPurchased = false)
                    )
                }
            }
            playBillingClient.getIdsOfPurchasedProducts()
                .onSuccess { purchasedIds ->
                    purchasedIds.onEach { onCoffeePurchased(it) }
                }
        }
    }

    fun onCoffeeClicked(activity: Activity, productId: String) {
        viewModelScope.launch {
            playBillingClient.purchaseProduct(activity, productId)
                .onSuccess { onCoffeePurchased(it) }
                .onFailure { showSnack(it.message.orEmpty()) }
        }
    }

    private suspend fun onCoffeePurchased(productId: String) {
        coffeeDao.insert(
            Coffee(id = productId, isPurchased = true)
        )
    }
}
