package my.cardholder.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.ActivityMainBinding
import my.cardholder.billing.BillingActivity
import my.cardholder.data.model.AppTheme
import my.cardholder.data.model.Card
import my.cardholder.shortcut.CardShortcutManager
import my.cardholder.util.ext.collectWhenStarted
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BillingActivity() {

    private companion object {
        const val FADE_IN_ANIM_DELAY_MS = 1500L
        const val FADE_IN_ANIM_DURATION_MS = 1000L
        const val NO_CARD_ID = -1L
    }

    private val destinationIdsWithBottomNav = setOf(
        R.id.permission_fragment,
        R.id.card_scan_fragment,
        R.id.card_list_fragment,
        R.id.settings_fragment,
    )

    @Inject
    lateinit var cardShortcutManager: CardShortcutManager

    private val viewModel: MainViewModel by viewModels()

    private var navController: NavController? = null

    private var bottomNavView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        installSplashScreen()

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            val navController = mainNavHost.getFragment<NavHostFragment>().navController
            this@MainActivity.navController = navController
            bottomNavView = mainBottomNavView
            mainBottomNavView.setupWithNavController(navController)
            navController.addOnDestinationChangedListener { _, destination, arguments ->
                // Scan screens opened from card editing (with a card_id) are not top-level screens.
                val isScanningForExistingCard =
                    (arguments?.getLong("card_id", Card.NEW_CARD_ID) ?: Card.NEW_CARD_ID) != Card.NEW_CARD_ID
                mainBottomNavView.isVisible =
                    destinationIdsWithBottomNav.contains(destination.id) && !isScanningForExistingCard
            }
        }
        // On recreation (e.g. rotation) the launch intent is still set, but the nav state is already restored.
        if (savedInstanceState == null) {
            handleShortcutIntent(intent)
        }
        cardShortcutManager.publishScanShortcut()

        collectWhenStarted(viewModel.appTheme) { theme ->
            setAppTheme(theme)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShortcutIntent(intent)
    }

    private fun handleShortcutIntent(intent: Intent) {
        if (intent.action == CardShortcutManager.ACTION_SCAN_CARD) {
            // Selecting the tab goes through the same NavigationUI path as a tap on it.
            bottomNavView?.selectedItemId = R.id.card_scan_fragment
            return
        }
        val cardId = intent.getLongExtra(CardShortcutManager.EXTRA_CARD_ID, NO_CARD_ID)
        if (cardId == NO_CARD_ID) return
        navController?.navigate(R.id.card_display_fragment, bundleOf("card_id" to cardId))
    }

    private fun setAppTheme(theme: AppTheme) {
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                AppTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }
}
