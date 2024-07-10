package my.cardholder.billing

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.rustore.sdk.billingclient.utils.resolveForBilling
import ru.rustore.sdk.core.feature.model.FeatureAvailabilityResult
import javax.inject.Inject

abstract class BillingActivity : AppCompatActivity(), BillingActivityInterface {

    @Inject
    lateinit var ruStoreBillingAssistant: RuStoreBillingAssistant

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            ruStoreBillingAssistant.billingClient.onNewIntent(intent)
        }
        ruStoreBillingAssistant.initialize()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        ruStoreBillingAssistant.billingClient.onNewIntent(intent)
    }

    override fun purchaseProduct(productId: String) {
        ruStoreBillingAssistant.billingClient.purchases.checkPurchasesAvailability()
            .addOnSuccessListener { result ->
                when (result) {
                    is FeatureAvailabilityResult.Available -> launchBillingFlow(productId)
                    is FeatureAvailabilityResult.Unavailable -> result.cause.resolveForBilling(this)
                }
            }
            .addOnFailureListener { throwable ->
                showToast(throwable.message)
            }
    }

    private fun launchBillingFlow(productId: String) {
        ruStoreBillingAssistant.getBillingFlowParams(productId) { result ->
            result.onSuccess { billingParams ->
                ruStoreBillingAssistant.billingClient.purchases.purchaseProduct(
                    productId = billingParams.productId,
                    orderId = billingParams.orderId,
                    quantity = billingParams.quantity,
                    developerPayload = null,
                ).addOnSuccessListener { paymentResult ->
                    paymentResult.toSimpleResult(productId)
                        .onSuccess { ruStoreBillingAssistant.initialize() }
                        .onFailure { showToast(it.message) }
                }.addOnFailureListener { throwable ->
                    showToast(throwable.message)
                }
            }
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}
