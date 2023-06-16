package my.cardholder.util.ext

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails

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

suspend fun BillingClient.queryNonConsumableProductDetails(
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
