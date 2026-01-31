package my.cardholder.billing

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.rustore.sdk.pay.IntentInteractor
import ru.rustore.sdk.pay.model.PreferredPurchaseType
import javax.inject.Inject

abstract class BillingActivity : AppCompatActivity(), BillingActivityInterface {

    @Inject
    lateinit var ruStoreBillingAssistant: RuStoreBillingAssistant

    private val intentInteractor: IntentInteractor by lazy {
        ruStoreBillingAssistant.billingClient.getIntentInteractor()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            intentInteractor.proceedIntent(intent)
        }
        ruStoreBillingAssistant.initialize()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intentInteractor.proceedIntent(intent)
    }

    override fun purchaseProduct(productId: String) {
        ruStoreBillingAssistant.getBillingFlowParams(productId) { result ->
            result.onSuccess { billingParams ->
                ruStoreBillingAssistant.billingClient.getPurchaseInteractor()
                    .purchase(
                        params = billingParams,
                        preferredPurchaseType = PreferredPurchaseType.ONE_STEP,
                    )
                    .addOnSuccessListener {
                        ruStoreBillingAssistant.initialize()
                    }
                    .addOnFailureListener { throwable ->
                        showToast(throwable.message)
                    }
            }
        }
    }

    private fun showToast(message: String?) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}
