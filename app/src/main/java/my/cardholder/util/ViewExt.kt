package my.cardholder.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.viewbinding.ViewBinding

fun ViewBinding.setupUniqueTransitionNames(
    uniqueSuffix: Long,
    vararg views: View,
) {
    views.onEach { view ->
        val uniqueName = view.transitionName.format(uniqueSuffix)
        ViewCompat.setTransitionName(view, uniqueName)
    }
}
