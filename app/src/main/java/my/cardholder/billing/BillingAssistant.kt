// package my.cardholder.billing

// import kotlinx.coroutines.channels.Channel
// import kotlinx.coroutines.flow.Flow
// import kotlinx.coroutines.flow.receiveAsFlow

// typealias ProductId = String

// abstract class BillingAssistant<BC, FP> : PurchasedProductsProvider {

//     protected val purchasedProductsChannel = Channel<List<ProductId>>(Channel.UNLIMITED)

//     override val purchasedProducts: Flow<List<ProductId>> = purchasedProductsChannel.receiveAsFlow()

//     abstract val billingClient: BC

//     abstract fun initialize()

//     abstract fun getBillingFlowParams(productId: ProductId, onResult: (Result<FP>) -> Unit)
// }
