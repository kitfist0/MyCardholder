package my.cardholder.ui.settings.coffee

import android.app.Activity
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import my.cardholder.BuildConfig
import my.cardholder.data.CoffeeDao
import my.cardholder.data.PlayBillingClient
import my.cardholder.data.model.Coffee
import my.cardholder.ui.base.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsCoffeeViewModel @Inject constructor(
    private val coffeeDao: CoffeeDao,
    private val playBillingClient: PlayBillingClient,
) : BaseViewModel() {

    val coffees = coffeeDao.getCoffees()

    init {
        viewModelScope.launch {
            if (coffeeDao.isEmpty()) {
                BuildConfig.PRODUCT_IDS.onEach { productId ->
                    coffeeDao.insert(Coffee(productId, productId.split(".")[1].capitalize(), false))
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
        coffeeDao.insert(Coffee(productId, productId.split(".")[1].capitalize(), true))
    }
}
