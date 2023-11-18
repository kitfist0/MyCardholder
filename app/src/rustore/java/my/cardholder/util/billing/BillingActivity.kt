package my.cardholder.util.billing

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        ruStoreBillingAssistant.billingClient.onNewIntent(intent)
    }

    override fun purchaseProduct(productId: String) {
        ruStoreBillingAssistant.apply {
            getBillingFlowParams(productId) { result ->
                result.onSuccess { billingParams ->
                    billingClient.purchases.purchaseProduct(
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
    }

    private fun showToast(message: String?) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}
