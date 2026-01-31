package my.cardholder.billing

import ru.rustore.sdk.pay.RuStorePayClient
import ru.rustore.sdk.pay.model.OrderId
import ru.rustore.sdk.pay.model.ProductPurchase
import ru.rustore.sdk.pay.model.ProductPurchaseParams
import ru.rustore.sdk.pay.model.ProductPurchaseStatus
import ru.rustore.sdk.pay.model.ProductType
import ru.rustore.sdk.pay.model.Quantity
import java.util.UUID

class RuStoreBillingAssistant() : BillingAssistant<RuStorePayClient, ProductPurchaseParams>() {

    override val billingClient = RuStorePayClient.instance

    override fun initialize() {
        handlePurchases()
    }

    override fun getBillingFlowParams(
        productId: ProductId,
        onResult: (Result<ProductPurchaseParams>) -> Unit
    ) {
        onResult.invoke(
            Result.success(
                ProductPurchaseParams(
                    productId = ru.rustore.sdk.pay.model.ProductId(productId),
                    orderId = OrderId(UUID.randomUUID().toString()),
                    quantity = Quantity(1),
                )
            )
        )
    }

    private fun handlePurchases() {
        billingClient.getPurchaseInteractor()
            .getPurchases(
                productType = ProductType.NON_CONSUMABLE_PRODUCT,
                purchaseStatus = ProductPurchaseStatus.CONFIRMED
            )
            .addOnSuccessListener { purchases ->
                val purchasedProductIds = purchases.map { (it as ProductPurchase).productId.value }
                purchasedProductsChannel.trySend(purchasedProductIds)
            }
    }
}
