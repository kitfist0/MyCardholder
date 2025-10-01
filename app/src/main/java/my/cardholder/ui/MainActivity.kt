package my.cardholder.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.ActivityMainBinding
import my.cardholder.billing.BillingActivity
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class MainActivity : BillingActivity() {

    private companion object {
        const val FADE_IN_ANIM_DELAY_MS = 1500L
        const val FADE_IN_ANIM_DURATION_MS = 1000L
    }

    private val destinationIdsWithBottomNav = setOf(
        R.id.permission_fragment,
        R.id.card_scan_fragment,
        R.id.card_list_fragment,
        R.id.settings_fragment,
    )

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
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

        collectWhenStarted(viewModel.nightModeEnabled) { isEnabled ->
            setDefaultNightMode(isEnabled)
        }

        collectWhenStarted(viewModel.backupDownloadLog) { logMessage ->
            binding.mainBottomNavMessageText.apply {
                if (logMessage.isNullOrEmpty()) {
                    this.animate()
                        .alpha(0f)
                        .setStartDelay(FADE_IN_ANIM_DELAY_MS)
                        .setDuration(FADE_IN_ANIM_DURATION_MS)
                        .withEndAction {
                            isVisible = false
                            text = null
                        }
                } else {
                    isVisible = true
                    text = logMessage
                }
            }
        }
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
