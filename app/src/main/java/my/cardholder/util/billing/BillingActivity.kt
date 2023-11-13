package my.cardholder.util.billing

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

abstract class BillingActivity : AppCompatActivity() {

    @Inject
    lateinit var googlePlayBillingAssistant: GooglePlayBillingAssistant

    @Inject
    lateinit var ruStoreBillingAssistant: RuStoreBillingAssistant

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googlePlayBillingAssistant.initialize()
        if (savedInstanceState == null) {
            ruStoreBillingAssistant.billingClient.onNewIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        ruStoreBillingAssistant.billingClient.onNewIntent(intent)
    }
}
