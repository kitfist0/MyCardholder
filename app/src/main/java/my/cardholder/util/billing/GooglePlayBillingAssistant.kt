package my.cardholder.util.billing

import android.app.Activity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import java.lang.ref.WeakReference

class GooglePlayBillingAssistant constructor(
    private val googlePlayBillingWrapper: GooglePlayBillingWrapper,
) : BillingAssistant {

    private val purchasedProductsChannel = Channel<List<String>>(Channel.UNLIMITED)

    override val purchasedProductIds: Flow<List<String>> = purchasedProductsChannel.receiveAsFlow()
        .onStart {
            googlePlayBillingWrapper.getClient()
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
        googlePlayBillingWrapper.getClient()
            .onSuccess { client ->
                val activity = activityReference.get()
                    ?: return Result.failure(Throwable("No activity reference"))
                client.launchNonConsumableProductPurchase(activity, productId)
                    .onSuccess {
                        val purchasesResult = googlePlayBillingWrapper.purchasesResultChannel.receive()
                        if (purchasesResult.billingResult.isOk()) {
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
}
