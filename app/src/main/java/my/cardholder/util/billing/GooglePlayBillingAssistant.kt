package my.cardholder.util.billing

import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

class GooglePlayBillingAssistant constructor(
    billingClientBuilder: BillingClient.Builder,
) : BillingAssistant<BillingClient, BillingFlowParams>(), PurchasesUpdatedListener {

    override val billingClient: BillingClient = billingClientBuilder
        .setListener(this)
        .enablePendingPurchases()
        .build()

    override fun initialize() {
        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                }

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.isOk()) {
                        handlePurchases()
                    }
                }
            }
        )
    }

    override fun getBillingFlowParams(productId: String, onResult: (Result<BillingFlowParams>) -> Unit) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val productDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient.queryProductDetailsAsync(productDetailsParams) { billingResult, productDetailsList ->
            val productDetailsParamsList =
                if (billingResult.isOk() && productDetailsList.isNotEmpty()) {
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetailsList.first())
                            .build()
                    )
                } else {
                    emptyList()
                }
            if (productDetailsParamsList.isEmpty()) {
                onResult.invoke(
                    Result.failure(Throwable(billingResult.getErrorMessage() ?: "Unknown error"))
                )
            } else {
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
                onResult.invoke(Result.success(billingFlowParams))
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.isOk() && purchases?.isNotEmpty() == true) {
            handlePurchases()
        }
    }

    private fun handlePurchases() {
        val queryPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(queryPurchasesParams) { billingResult, purchases ->
            if (billingResult.isOk() && purchases.isNotEmpty()) {
                val purchasedIds = purchases
                    .filter { it.isPurchased() }
                    .also { acknowledgePurchases(it) }
                    .flatMap { purchase -> purchase.products }
                purchasedProductIdsChannel.trySend(purchasedIds)
            }
        }
    }

    private fun acknowledgePurchases(purchases: List<Purchase>) {
        purchases.forEach { purchase ->
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) {
            }
        }
    }
}
