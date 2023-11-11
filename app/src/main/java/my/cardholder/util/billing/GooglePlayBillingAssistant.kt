package my.cardholder.util.billing

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GooglePlayBillingAssistant constructor(
    billingClientBuilder: BillingClient.Builder,
    private val billingConnectionMutex: Mutex = Mutex()
) : BillingAssistant {

    private val purchasesResultChannel = Channel<PurchasesResult>(Channel.UNLIMITED)

    private val billingClient = billingClientBuilder
        .setListener { billingResult, purchases ->
            purchasesResultChannel.trySend(PurchasesResult(billingResult, purchases.orEmpty()))
        }
        .enablePendingPurchases()
        .build()

    private val purchasedProductsChannel = Channel<List<String>>(Channel.UNLIMITED)

    override val purchasedProductIds: Flow<List<String>> = purchasedProductsChannel.receiveAsFlow()
        .onStart {
            getClient()
                .onSuccess { client ->
                    client.queryAndAcknowledgePurchasedProducts()
                        .onSuccess { ids -> purchasedProductsChannel.send(ids) }
                }
        }

    override suspend fun purchaseProduct(
        activityReference: WeakReference<Activity>,
        productId: String
    ): Result<String> {
        var throwable: Throwable? = null
        getClient()
            .onSuccess { client ->
                val activity = activityReference.get()
                    ?: return Result.failure(Throwable("No activity reference"))
                client.launchNonConsumableProductPurchase(activity, productId)
                    .onSuccess {
                        if (purchasesResultChannel.receive().billingResult.isOk()) {
                            client.queryAndAcknowledgePurchasedProducts()
                                .onSuccess { ids -> purchasedProductsChannel.send(ids) }
                                .onFailure { throwable = it }
                        }
                    }
                    .onFailure { throwable = it }
            }
            .onFailure { throwable = it }
        return throwable?.let { Result.failure(it) } ?: Result.success(productId)
    }

    /**
     * Returns BillingClient if it is already connected, otherwise
     * initiates the connection and suspends until this client is connected.
     * If a connection is already in the process of being established, this
     * method just suspends until the billing client is ready
     */
    private suspend fun getClient(): Result<BillingClient> =
        billingConnectionMutex.withLock {
            return runCatching {
                if (!billingClient.isReady) {
                    billingClient.connectOrThrow()
                }
                billingClient
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
                    // No need to setup reconnection logic here, call getClient()
                    // before each purchase to reconnect as necessary
                }
            }
        )
    }
}
