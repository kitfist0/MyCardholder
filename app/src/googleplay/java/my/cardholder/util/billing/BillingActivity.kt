package my.cardholder.util.billing

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

abstract class BillingActivity : AppCompatActivity(), BillingActivityInterface {

    @Inject
    lateinit var googlePlayBillingAssistant: GooglePlayBillingAssistant

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googlePlayBillingAssistant.initialize()
    }

    override fun purchaseProduct(productId: String) {
        googlePlayBillingAssistant.apply {
            getBillingFlowParams(productId) { result ->
                result.onSuccess {
                    billingClient.launchBillingFlow(this@BillingActivity, it)
                }.onFailure {
                    Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
