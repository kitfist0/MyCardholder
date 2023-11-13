package my.cardholder.util.billing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BillingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeBilling()
    }

    protected abstract fun initializeBilling()

    abstract fun purchaseProduct(productId: String)
}
