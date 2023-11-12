package my.cardholder.util.billing

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import javax.inject.Inject

abstract class BillingActivity : AppCompatActivity() {

    @Inject
    lateinit var ruStoreBillingClient: RuStoreBillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            ruStoreBillingClient.onNewIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        ruStoreBillingClient.onNewIntent(intent)
    }
}
