package my.cardholder.data.source.remote

import android.app.Activity
import com.android.billingclient.api.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class PlayBillingClient @Inject constructor(
    billingClientBuilder: BillingClient.Builder,
) {

    private val purchasesResultChannel = Channel<PurchasesResult>(Channel.UNLIMITED)

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        purchasesResultChannel.trySend(PurchasesResult(billingResult, purchases.orEmpty()))
    }

    private val billingClient = billingClientBuilder
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    private val billingConnectionMutex = Mutex()

    val productIds = listOf("coffee.espresso", "coffee.cappuccino", "coffee.latte")

    suspend fun purchaseProduct(activity: Activity, productId: String): Result<String> {
        billingClient.ensureReady().exceptionOrNull()?.let { exception ->
            return Result.failure(exception)
        }
        val productDetailsResult = queryNonConsumableProductDetails()
        return productDetailsResult.productDetailsList
            ?.find { it.productId == productId }
            ?.let { productDetails ->
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
                billingClient.launchBillingFlow(activity, billingFlowParams)
                val purchasesResult = purchasesResultChannel.receive()
                if (purchasesResult.billingResult.isOk()) {
                    acknowledgePurchasedProducts()
                    Result.success(productId)
                } else {
                    Result.failure(Throwable(purchasesResult.billingResult.getErrorMessage()))
                }
            }
            ?: Result.failure(Throwable(productDetailsResult.billingResult.getErrorMessage()))
    }

    suspend fun getIdsOfPurchasedProducts(): Result<List<String>> {
        billingClient.ensureReady().exceptionOrNull()?.let { exception ->
            return Result.failure(exception)
        }
        val purchasesResult = queryNonConsumablePurchases()
        return if (purchasesResult.billingResult.isOk()) {
            val purchasedIds = purchasesResult.purchasesList
                .filter { purchase -> purchase.purchaseState == Purchase.PurchaseState.PURCHASED }
                .flatMap { purchase -> purchase.products }
            Result.success(purchasedIds)
        } else {
            Result.failure(Throwable(purchasesResult.billingResult.getErrorMessage()))
        }
    }

    private suspend fun queryNonConsumableProductDetails(): ProductDetailsResult {
        val productList = productIds.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        return billingClient.queryProductDetails(queryProductDetailsParams)
    }

    private suspend fun acknowledgePurchasedProducts() {
        queryNonConsumablePurchases().purchasesList
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
            .onEach {
                billingClient.acknowledgePurchase(
                    AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(it.purchaseToken)
                        .build()
                )
            }
    }

    private suspend fun queryNonConsumablePurchases(): PurchasesResult {
        val queryPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        return billingClient.queryPurchasesAsync(queryPurchasesParams)
    }

    /**
     * Returns immediately if this BillingClient is already connected, otherwise
     * initiates the connection and suspends until this client is connected.
     * If a connection is already in the process of being established, this
     * method just suspends until the billing client is ready
     */
    private suspend fun BillingClient.ensureReady(): Result<Boolean> =
        billingConnectionMutex.withLock {
            return runCatching {
                if (!isReady) {
                    connectOrThrow()
                }
                true
            }
        }

    private suspend fun BillingClient.connectOrThrow() = suspendCoroutine { continuation ->
        startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.isOk()) {
                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(RuntimeException(result.getErrorMessage()))
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // No need to setup reconnection logic here, call ensureReady()
                    // before each purchase to reconnect as necessary
                }
            }
        )
    }

    private fun BillingResult.getErrorMessage() = when (responseCode) {
        BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> "Service timeout"
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

    private fun BillingResult.isOk() = responseCode == BillingClient.BillingResponseCode.OK
}
