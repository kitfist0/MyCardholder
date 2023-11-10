package my.cardholder.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.ActivityMainBinding
import my.cardholder.util.ext.collectWhenStarted
import ru.rustore.sdk.billingclient.RuStoreBillingClient
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val destinationIdsWithBottomNav = setOf(
        R.id.permission_fragment,
        R.id.card_scan_fragment,
        R.id.card_list_fragment,
        R.id.settings_fragment,
    )

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var ruStoreBillingClient: RuStoreBillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            ruStoreBillingClient.onNewIntent(intent)
        }
        installSplashScreen()

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            val navController = mainNavHost.getFragment<NavHostFragment>().navController
            mainBottomNavView.setupWithNavController(navController)
            navController.addOnDestinationChangedListener { _, destination, _ ->
                mainBottomNavView.isVisible = destinationIdsWithBottomNav.contains(destination.id)
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        collectWhenStarted(viewModel.nightModeEnabled) { isEnabled ->
            setDefaultNightMode(isEnabled)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        ruStoreBillingClient.onNewIntent(intent)
    }

    private fun setDefaultNightMode(isEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
