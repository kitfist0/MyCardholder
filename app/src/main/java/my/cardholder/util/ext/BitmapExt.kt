package my.cardholder.util.ext

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import androidx.core.graphics.get
import androidx.core.graphics.toColorInt
import com.google.mlkit.vision.barcode.common.Barcode
import my.cardholder.data.model.Card

private const val DEFAULT_SAMPLE_STEP = 8

/**
 * Determines the dominant background color surrounding [barcode] in this image (i.e. the color
 * of the physical card the code is printed on), excluding the barcode's own black/white pattern,
 * then matches it to the closest color available for cards.
 */
fun Bitmap.detectBackgroundCardColor(barcode: Barcode): String? {
    val backgroundArgb = getAverageColor(excludeRect = barcode.boundingBox)
    return backgroundArgb?.let { findClosestColor(it) }
}

private fun findClosestColor(argb: Int): String {
    return Card.COLORS.minBy { colorDistance(it.toColorInt(), argb) }
}

private fun colorDistance(first: Int, second: Int): Int {
    val redDiff = Color.red(first) - Color.red(second)
    val greenDiff = Color.green(first) - Color.green(second)
    val blueDiff = Color.blue(first) - Color.blue(second)
    return redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff
}

/**
 * Averages pixel colors, skipping [excludeRect] (e.g. the barcode itself) so the result reflects
 * the surrounding background rather than the black/white barcode pattern.
 */
fun Bitmap.getAverageColor(excludeRect: Rect?, sampleStep: Int = DEFAULT_SAMPLE_STEP): Int? {
    var sumRed = 0L
    var sumGreen = 0L
    var sumBlue = 0L
    var sampleCount = 0

    var y = 0
    while (y < height) {
        var x = 0
        while (x < width) {
            if (excludeRect == null || !excludeRect.contains(x, y)) {
                val pixel = get(x, y)
                sumRed += Color.red(pixel)
                sumGreen += Color.green(pixel)
                sumBlue += Color.blue(pixel)
                sampleCount++
            }
            x += sampleStep
        }
        y += sampleStep
    }

    if (sampleCount == 0) {
        return null
    }
    return Color.rgb(
        (sumRed / sampleCount).toInt(),
        (sumGreen / sampleCount).toInt(),
        (sumBlue / sampleCount).toInt(),
    )
}
