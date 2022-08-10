package my.cardholder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val appBarConfiguration by lazy {
        AppBarConfiguration(
            setOf(
                R.id.navigation_scanner,
                R.id.navigation_cards,
                R.id.navigation_settings,
            )
        )
    }

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment).navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.mainBottomNavView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_card, R.id.navigation_card_editor ->
                    binding.mainBottomNavView.isVisible = false
                else -> binding.mainBottomNavView.isVisible = true
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }
}
