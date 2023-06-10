package my.cardholder.util.ext

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult

fun BillingResult.getErrorMessage() = when (responseCode) {
    BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> "Service timeout"
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
    else -> "Unknown error"
}

fun BillingResult.isOk() = responseCode == BillingClient.BillingResponseCode.OK
