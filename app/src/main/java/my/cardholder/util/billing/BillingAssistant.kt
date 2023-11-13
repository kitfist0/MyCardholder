package my.cardholder.util.billing

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BillingAssistant<BC, FP> {

    protected val purchasedProductIdsChannel = Channel<List<String>>(Channel.UNLIMITED)
    val purchasedProductIds: Flow<List<String>> = purchasedProductIdsChannel.receiveAsFlow()

    abstract val billingClient: BC

    abstract fun initialize()

    abstract fun getBillingFlowParams(productId: String, onResult: (Result<FP>) -> Unit)
}
