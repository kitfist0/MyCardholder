package my.cardholder.util

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import my.cardholder.util.ext.getErrorMessage
import my.cardholder.util.ext.isOk
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class PlayBillingWrapper constructor(
    billingClientBuilder: BillingClient.Builder,
    private val billingConnectionMutex: Mutex = Mutex()
) {

    private val billingClient = billingClientBuilder
        .setListener { billingResult, purchases ->
            purchasesResultChannel.trySend(PurchasesResult(billingResult, purchases.orEmpty()))
        }
        .enablePendingPurchases()
        .build()

    val purchasesResultChannel = Channel<PurchasesResult>(Channel.UNLIMITED)

    /**
     * Returns BillingClient if it is already connected, otherwise
     * initiates the connection and suspends until this client is connected.
     * If a connection is already in the process of being established, this
     * method just suspends until the billing client is ready
     */
    suspend fun getClient(): Result<BillingClient> =
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
