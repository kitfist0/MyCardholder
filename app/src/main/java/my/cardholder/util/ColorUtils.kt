package my.cardholder.util

import android.graphics.Color
import androidx.core.graphics.toColorInt

/** Returns the color from [candidates] that is visually closest to [argb]. */
fun findClosestColor(candidates: Array<String>, argb: Int): String {
    return candidates.minBy { colorDistance(it.toColorInt(), argb) }
}

private fun colorDistance(first: Int, second: Int): Int {
    val redDiff = Color.red(first) - Color.red(second)
    val greenDiff = Color.green(first) - Color.green(second)
    val blueDiff = Color.blue(first) - Color.blue(second)
    return redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff
}
