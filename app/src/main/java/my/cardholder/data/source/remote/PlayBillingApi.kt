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

    suspend fun getBillingFlowParams(productId: String): Result<BillingFlowParams?> {
        return playBillingWrapper.getClient().fold(
            onSuccess = { billingClient ->
                val productDetailsResult = billingClient.queryNonConsumableProductDetails()
                val billingFlowParams = productDetailsResult.productDetailsList
                    ?.find { it.productId == productId }
                    ?.let { productDetails ->
                        val productDetailsParamsList = listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                        )
                        BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build()
                    }
                Result.success(billingFlowParams)
            },
            onFailure = {
                Result.failure(it)
            }
        )
    }

    suspend fun processBillingFlowResult(billingResult: BillingResult): Result<Boolean> {
        if (!billingResult.isOk()) {
            return Result.failure(Throwable(billingResult.getErrorMessage()))
        }
        val purchasesResult = playBillingWrapper.purchasesResultChannel.receive()
        return if (purchasesResult.billingResult.isOk()) {
            playBillingWrapper.getClientOrNull()
                ?.acknowledgePurchasesIfRequired(purchasesResult.purchasesList)
            Result.success(true)
        } else {
            Result.failure(Throwable(purchasesResult.billingResult.getErrorMessage()))
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

    private suspend fun BillingClient.queryNonConsumableProductDetails(
        vararg productIds: String,
    ): ProductDetailsResult {
        val productList = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        return queryProductDetails(queryProductDetailsParams)
    }

    private suspend fun BillingClient.queryNonConsumablePurchases(): PurchasesResult {
        val queryPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        return queryPurchasesAsync(queryPurchasesParams)
    }

    private fun Purchase.isPurchased() = purchaseState == Purchase.PurchaseState.PURCHASED
}
