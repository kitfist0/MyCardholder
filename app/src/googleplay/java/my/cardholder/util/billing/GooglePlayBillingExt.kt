package my.cardholder.util.billing

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase

fun Purchase.isPurchased() = purchaseState == Purchase.PurchaseState.PURCHASED

fun BillingResult.isOk() = responseCode == BillingClient.BillingResponseCode.OK

fun BillingResult.getErrorMessage(): String? = when (responseCode) {
    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> "Feature not supported"
    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> "Service disconnected"
    BillingClient.BillingResponseCode.USER_CANCELED -> "User canceled"
    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> "Service unavailable"
    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> "Billing unavailable"
    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> "Item unavailable"
    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> "Developer error"
    BillingClient.BillingResponseCode.ERROR -> "Error"
    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> "Item already owned"
    BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> "Item not owned"
    else -> null
}
