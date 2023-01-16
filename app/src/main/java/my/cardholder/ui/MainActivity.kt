package my.cardholder.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import my.cardholder.R
import my.cardholder.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = binding.mainNavHost.getFragment<NavHostFragment>().navController
        binding.mainBottomNavView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.viewer_fragment,
                R.id.editor_fragment,
                R.id.search_fragment,
                R.id.specs_fragment ->
                    binding.mainBottomNavView.isVisible = false
                else ->
                    binding.mainBottomNavView.isVisible = true
            }
        }
    }
}
