package my.cardholder.util.billing

import android.widget.Toast
import javax.inject.Inject

abstract class GooglePlayBillingActivity : BillingActivity() {

    @Inject
    lateinit var googlePlayBillingAssistant: GooglePlayBillingAssistant

    override fun initializeBilling() {
        googlePlayBillingAssistant.initialize()
    }

    override fun purchaseProduct(productId: String) {
        googlePlayBillingAssistant.apply {
            getBillingFlowParams(productId) { result ->
                result.onSuccess {
                    billingClient.launchBillingFlow(this@GooglePlayBillingActivity, it)
                }.onFailure {
                    Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
