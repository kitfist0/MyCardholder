package my.cardholder.util

import android.app.Activity
import my.cardholder.util.ext.launchNonConsumableProductPurchase
import javax.inject.Inject

class PurchaseFlowLauncher @Inject constructor(
    private val playBillingWrapper: PlayBillingWrapper,
) {
    suspend fun startPurchase(activity: Activity, productId: String): Result<Unit> {
        return playBillingWrapper.getClient().fold(
            onSuccess = { billingClient ->
                billingClient.launchNonConsumableProductPurchase(activity, productId)
            },
            onFailure = { throwable ->
                Result.failure(throwable)
            }
        )
    }
}
