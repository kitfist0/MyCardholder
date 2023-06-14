package my.cardholder.data.source.remote

import com.android.billingclient.api.*
import my.cardholder.util.PlayBillingWrapper
import my.cardholder.util.ext.getErrorMessage
import my.cardholder.util.ext.isOk
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayBillingApi @Inject constructor(
    private val playBillingWrapper: PlayBillingWrapper,
) {

    val productIds = listOf("coffee.espresso", "coffee.cappuccino", "coffee.latte")

    suspend fun waitProductPurchaseResult(): Result<Boolean> {
        val purchasesResult = playBillingWrapper.purchasesResultChannel.receive()
        val billingResult = purchasesResult.billingResult
        val purchasesList = purchasesResult.purchasesList
        return if (billingResult.isOk() && purchasesList.isNotEmpty()) {
            playBillingWrapper.getClientOrNull()
                ?.acknowledgePurchasesIfRequired(purchasesList)
            Result.success(true)
        } else {
            Result.failure(Throwable(billingResult.getErrorMessage()))
        }
    }

    suspend fun getIdsOfPurchasedProducts(): Result<List<String>> {
        return playBillingWrapper.getClient().fold(
            onSuccess = { billingClient ->
                val purchasesResult = billingClient.queryNonConsumablePurchases()
                if (purchasesResult.billingResult.isOk()) {
                    val purchasedPurchases = purchasesResult.purchasesList
                        .filter { it.isPurchased() }
                    billingClient.acknowledgePurchasesIfRequired(purchasedPurchases)
                    val purchasedProductIds = purchasedPurchases
                        .flatMap { purchase -> purchase.products }
                    Result.success(purchasedProductIds)
                } else {
                    Result.failure(Throwable(purchasesResult.billingResult.getErrorMessage()))
                }
            },
            onFailure = {
                Result.failure(it)
            }
        )
    }

    private suspend fun BillingClient.acknowledgePurchasesIfRequired(
        purchasesList: List<Purchase>
    ) {
        purchasesList.filter { purchase -> purchase.isPurchased() && !purchase.isAcknowledged }
            .onEach { purchase ->
                acknowledgePurchase(
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                )
            }
    }

    private suspend fun BillingClient.queryNonConsumablePurchases(): PurchasesResult {
        val queryPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        return queryPurchasesAsync(queryPurchasesParams)
    }

    private fun Purchase.isPurchased() = purchaseState == Purchase.PurchaseState.PURCHASED
}
