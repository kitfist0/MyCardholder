package my.cardholder.ui.coffee

import com.android.billingclient.api.BillingFlowParams
import my.cardholder.data.model.Coffee

data class CoffeeState(
    val coffees: List<Coffee>,
    val launchBillingFlowRequest: BillingFlowParams?,
)
