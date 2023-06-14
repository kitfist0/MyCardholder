package my.cardholder.util

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import dagger.hilt.android.scopes.ActivityScoped
import my.cardholder.util.ext.getErrorMessage
import my.cardholder.util.ext.isOk
import javax.inject.Inject

@ActivityScoped
class PurchaseFlowLauncher @Inject constructor(
    private val activity: Activity,
    private val playBillingWrapper: PlayBillingWrapper,
) {

    suspend fun startPurchase(productId: String): Result<Unit> {
        return playBillingWrapper.getClient().fold(
            onSuccess = { billingClient ->
                val productDetailsResult = billingClient.queryNonConsumableProductDetails(productId)
                val productDetailsBillingResult = productDetailsResult.billingResult
                val productDetailsList = productDetailsResult.productDetailsList
                if (productDetailsBillingResult.isOk() && !productDetailsList.isNullOrEmpty()) {
                    billingClient.launchBillingFlowForResult(productDetailsList.first())
                } else {
                    Result.failure(Throwable(productDetailsBillingResult.getErrorMessage()))
                }
            },
            onFailure = {
                Result.failure(it)
            }
        )
    }

    private fun BillingClient.launchBillingFlowForResult(
        productDetails: ProductDetails,
    ): Result<Unit> {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .build()
        )
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
}
