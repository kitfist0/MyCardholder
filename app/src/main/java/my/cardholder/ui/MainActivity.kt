package my.cardholder.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.elevation.SurfaceColors
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val appBarConfiguration by lazy {
        AppBarConfiguration(
            setOf(
                R.id.scanner_navigation,
                R.id.cards_fragment,
                R.id.settings_fragment,
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
                R.id.card_viewer_fragment,
                R.id.card_editor_fragment, ->
                    binding.mainBottomNavView.isVisible = false
                else ->
                    binding.mainBottomNavView.isVisible = true
            }
        }
        setStatusBarAndNavBarColors()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    private fun setStatusBarAndNavBarColors() {
        val color = SurfaceColors.SURFACE_5.getColor(this)
        // Set color of system statusBar same as ActionBar
        window.statusBarColor = color
        // Set color of system navigationBar same as BottomNavigationView
        window.navigationBarColor = color
    }
}
