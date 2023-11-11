package my.cardholder.util.billing

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import java.lang.ref.WeakReference

interface BillingAssistant {

    val purchasedProductIds: Flow<List<String>>

    suspend fun purchaseProduct(
        activityReference: WeakReference<Activity>,
        productId: String,
    ): Result<String>
}
