package my.cardholder.data

import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import kotlinx.coroutines.flow.Flow
import my.cardholder.data.model.Coffee
import my.cardholder.data.source.local.CoffeeDao
import my.cardholder.data.source.remote.PlayBillingApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoffeeRepository @Inject constructor(
    private val coffeeDao: CoffeeDao,
    private val playBillingApi: PlayBillingApi,
) {

    val coffees: Flow<List<Coffee>> = coffeeDao.getCoffees()

    suspend fun initialize() {
        if (coffeeDao.isEmpty()) {
            val coffees = playBillingApi.productIds.map { productId ->
                Coffee(id = productId, isPurchased = false)
            }
            coffeeDao.upsert(coffees)
        }
        updatePurchaseStatusOfCoffees()
    }

    suspend fun getCoffeeBillingFlowParams(coffeeId: String): Result<BillingFlowParams?> {
        return playBillingApi.getBillingFlowParams(coffeeId)
    }

    suspend fun processCoffeeBillingFlowResult(billingResult: BillingResult): Result<Boolean> {
        return playBillingApi.processBillingFlowResult(billingResult)
            .onSuccess { updatePurchaseStatusOfCoffees() }
    }

    private suspend fun updatePurchaseStatusOfCoffees() {
        playBillingApi.getIdsOfPurchasedProducts()
            .onSuccess { productIds ->
                val coffees = productIds.map { productId ->
                    Coffee(id = productId, isPurchased = true)
                }
                coffeeDao.upsert(coffees)
            }
    }
}
