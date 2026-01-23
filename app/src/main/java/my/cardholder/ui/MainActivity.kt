package my.cardholder.ui

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
import my.cardholder.data.model.AppTheme
import my.cardholder.util.ext.collectWhenStarted

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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

        collectWhenStarted(viewModel.appTheme) { theme ->
            setAppTheme(theme)
        }
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