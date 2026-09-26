package my.cardholder.util.ext

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect

private const val DEFAULT_SAMPLE_STEP = 8

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
                val pixel = getPixel(x, y)
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
