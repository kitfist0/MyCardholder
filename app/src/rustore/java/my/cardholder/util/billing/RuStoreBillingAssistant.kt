package my.cardholder.util.billing

import ru.rustore.sdk.billingclient.RuStoreBillingClient
import ru.rustore.sdk.billingclient.model.purchase.PurchaseState
import java.util.UUID

data class RuStoreBillingFlowParams(
    val productId: String,
    val orderId: String = UUID.randomUUID().toString(),
    val quantity: Int = 1,
)

class RuStoreBillingAssistant constructor(
    override val billingClient: RuStoreBillingClient,
) : BillingAssistant<RuStoreBillingClient, RuStoreBillingFlowParams>() {

    override fun initialize() {
        handlePurchases()
    }

    override fun getBillingFlowParams(
        productId: ProductId,
        onResult: (Result<RuStoreBillingFlowParams>) -> Unit
    ) {
        onResult.invoke(
            Result.success(RuStoreBillingFlowParams(productId = productId))
        )
    }

    private fun handlePurchases() {
        billingClient.purchases.getPurchases()
            .addOnSuccessListener { purchases ->
                val purchasedProductIds = purchases
                    .filter { it.purchaseState == PurchaseState.CONFIRMED }
                    .map { it.productId }
                purchasedProductsChannel.trySend(purchasedProductIds)
            }
    }
}
