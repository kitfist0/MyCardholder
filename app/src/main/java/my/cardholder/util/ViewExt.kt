package my.cardholder.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.viewbinding.ViewBinding

fun ViewBinding.setupUniqueTransitionNamesAndReturnSharedElements(
    uniqueSuffix: Long,
    vararg viewIds: Int,
): Map<View, String> {
    val sharedElements = mutableMapOf<View, String>()
    viewIds.onEach { viewId ->
        val view = root.findViewById<View>(viewId)
        val uniqueName = view.transitionName.format(uniqueSuffix)
        ViewCompat.setTransitionName(view, uniqueName)
        sharedElements[view] = uniqueName
    }
    return sharedElements
}