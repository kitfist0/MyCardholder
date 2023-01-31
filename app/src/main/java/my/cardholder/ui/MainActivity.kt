package my.cardholder.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val destIdsWithoutBottomNav = mapOf(
        R.id.viewer_fragment to null,
        R.id.editor_fragment to null,
        R.id.search_fragment to null,
        R.id.delete_card_dialog to null,
        R.id.specs_fragment to null,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            val navController = mainNavHost.getFragment<NavHostFragment>().navController
            mainBottomNavView.setupWithNavController(navController)
            navController.addOnDestinationChangedListener { _, destination, _ ->
                mainBottomNavView.isVisible = !destIdsWithoutBottomNav.containsKey(destination.id)
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
