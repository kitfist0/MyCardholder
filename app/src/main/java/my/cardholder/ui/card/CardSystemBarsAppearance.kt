package my.cardholder.ui.card

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import my.cardholder.util.ContentColor

/**
 * Makes the system bar icons readable on the card color lying under them, while the screen is
 * shown: dark icons on a light card and light ones on a dark card, in either app theme.
 *
 * Switched on resume and restored on pause: a screen being left pauses before the next one resumes,
 * so the next screen's appearance is never overwritten.
 */
class CardSystemBarsAppearance(
    private val fragment: Fragment,
    private val isNavigationBarOnCard: Boolean = false,
) : DefaultLifecycleObserver {

    private var cardColor: Int? = null

    /** The appearance before this screen changed it, while the screen is resumed */
    private var originalAppearance: Pair<Boolean, Boolean>? = null

    init {
        fragment.lifecycle.addObserver(this)
    }

    fun setCardColor(color: Int) {
        cardColor = color
        if (originalAppearance != null) {
            apply()
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        val controller = controller() ?: return
        originalAppearance = controller.isAppearanceLightStatusBars to controller.isAppearanceLightNavigationBars
        apply()
    }

    override fun onPause(owner: LifecycleOwner) {
        val (lightStatusBars, lightNavigationBars) = originalAppearance ?: return
        controller()?.apply {
            isAppearanceLightStatusBars = lightStatusBars
            isAppearanceLightNavigationBars = lightNavigationBars
        }
        originalAppearance = null
    }

    private fun apply() {
        val color = cardColor ?: return
        val controller = controller() ?: return
        // "Light" bars are the ones with dark icons
        val darkIcons = ContentColor.prefersDarkContent(color)
        controller.isAppearanceLightStatusBars = darkIcons
        if (isNavigationBarOnCard) {
            controller.isAppearanceLightNavigationBars = darkIcons
        }
    }

    private fun controller(): WindowInsetsControllerCompat? {
        val window = fragment.activity?.window ?: return null
        return WindowCompat.getInsetsController(window, window.decorView)
    }
}
