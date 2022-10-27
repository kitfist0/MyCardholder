package my.cardholder.util

import android.view.View
import androidx.core.view.ViewCompat

fun View.setupUniqueTransitionName(uniqueSuffix: Long) {
    ViewCompat.setTransitionName(this, transitionName.format(uniqueSuffix))
}
