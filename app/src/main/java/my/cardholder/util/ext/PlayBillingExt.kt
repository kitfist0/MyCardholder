package my.cardholder.util.ext

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResult
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync

suspend fun BillingClient.launchNonConsumableProductPurchase(
    activity: Activity,
    productId: String,
): Result<Unit> {
    val productDetailsResult = queryNonConsumableProductDetails(productId)
    val productDetailsList = productDetailsResult.productDetailsList
    val productDetailsParamsList =
        if (productDetailsResult.billingResult.isOk() && productDetailsList?.isNotEmpty() == true) {
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetailsList.first())
                    .build()
            )
        } else {
            emptyList()
        }
    val billingFlowParams = BillingFlowParams.newBuilder()
        .setProductDetailsParamsList(productDetailsParamsList)
        .build()
    val billingResult = launchBillingFlow(activity, billingFlowParams)
    return if (billingResult.isOk()) {
        Result.success(Unit)
    } else {
        Result.failure(Throwable(billingResult.getErrorMessage()))
    }
}

suspend fun BillingClient.queryAndAcknowledgePurchasedProducts(): Result<List<String>> {
    val purchasesResult = queryNonConsumablePurchases()
    val purchasesList = purchasesResult.purchasesList
    return if (purchasesResult.billingResult.isOk()) {
        acknowledgePurchasesIfRequired(purchasesList)
        val purchasedProductIds = purchasesList
            .filter { it.isPurchased() }
            .flatMap { purchase -> purchase.products }
        Result.success(purchasedProductIds)
    } else {
        Result.failure(Throwable(purchasesResult.billingResult.getErrorMessage()))
    }
}

private suspend fun BillingClient.acknowledgePurchasesIfRequired(
    purchasesList: List<Purchase>,
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
    productId: String,
): ProductDetailsResult {
    val productList = listOf(
        QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productId)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
    )
    return queryProductDetails(
        QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
    )
}

private suspend fun BillingClient.queryNonConsumablePurchases(): PurchasesResult {
    val queryPurchasesParams = QueryPurchasesParams.newBuilder()
        .setProductType(BillingClient.ProductType.INAPP)
        .build()
    return queryPurchasesAsync(queryPurchasesParams)
}

private fun Purchase.isPurchased() = purchaseState == Purchase.PurchaseState.PURCHASED

fun BillingResult.isOk() = responseCode == BillingClient.BillingResponseCode.OK

fun BillingResult.getErrorMessage() = when (responseCode) {
    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> "Feature not supported"
    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> "Service disconnected"
    BillingClient.BillingResponseCode.USER_CANCELED -> "User canceled"
    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "Service unavailable"
    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "Billing unavailable"
    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "Item unavailable"
    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "Developer error"
    BillingClient.BillingResponseCode.ERROR -> "Error"
    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "Item already owned"
    BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "Item not owned"
    else -> "Unknown error"
}
