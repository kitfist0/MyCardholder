package my.cardholder.util.billing

import android.app.Activity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.model.purchase.PaymentResult
import ru.rustore.sdk.billingclient.model.purchase.PurchaseState
import ru.rustore.sdk.core.tasks.OnCompleteListener
import java.lang.ref.WeakReference
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RuStoreBillingAssistant constructor(
    private val ruStoreBillingClient: RuStoreBillingClient,
) : BillingAssistant {

    private val purchasedIdsChannel = Channel<List<String>>(Channel.UNLIMITED)

    override val purchasedProductIds: Flow<List<String>> = purchasedIdsChannel.receiveAsFlow()
        .onStart {
            queryPurchasedProducts()
        }

    override suspend fun purchaseProduct(
        activityReference: WeakReference<Activity>,
        productId: String
    ): Result<String> {
        return suspendCoroutine { cont ->
            val listener = object : OnCompleteListener<PaymentResult> {
                override fun onFailure(throwable: Throwable) {
                    cont.resume(Result.failure(throwable))
                }

                override fun onSuccess(result: PaymentResult) {
                    cont.resume(result.toSimpleResult(productId))
                }
            }
            ruStoreBillingClient.purchases.purchaseProduct(
                productId = productId,
                orderId = UUID.randomUUID().toString(),
                quantity = 1,
                developerPayload = null,
            ).addOnCompleteListener(listener)
        }
    }

    private fun queryPurchasedProducts() {
        ruStoreBillingClient.purchases.getPurchases()
            .addOnSuccessListener { purchases ->
                val purchasedProductIds = purchases
                    .filter { it.purchaseState == PurchaseState.CONFIRMED }
                    .map { it.productId }
                purchasedIdsChannel.trySend(purchasedProductIds)
            }
    }
}
