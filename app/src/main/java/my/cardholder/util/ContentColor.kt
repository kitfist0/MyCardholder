package my.cardholder.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * The color of text and icons on a background of any color, e.g. on a card. Like a Material
 * "on" color (the text of a button on its container), it is a dark or light shade of the
 * background's own hue and saturation: only its lightness changes, as little as needed for
 * the WCAG contrast of the text size, so that as much of the color as possible stays visible.
 *
 * Written without android.graphics.Color, so that it runs in unit tests.
 */
object ContentColor {

    /** WCAG minimum for normal text, e.g. the category label */
    const val NORMAL_TEXT_CONTRAST = 4.5

    /**
     * WCAG minimum for large text (from 18pt, or 14pt bold), e.g. the card name. On mid tone
     * backgrounds such as purple, it lets light text keep a visible tint instead of being near white.
     */
    const val LARGE_TEXT_CONTRAST = 3.0

    /** WCAG minimum for icons and other graphical objects */
    const val ICON_CONTRAST = 3.0

    /**
     * WCAG enhanced contrast. Grey backgrounds have no color to keep in the text, so a text
     * with the minimal contrast would only look dull on them.
     */
    const val GREY_BACKGROUND_CONTRAST = 7.0

    // Below this saturation a background looks grey
    private const val GREY_SATURATION = 0.1

    // Lightness (in percent) the search starts from: the most colorful dark and light shades
    private const val DARK_START_LIGHTNESS = 40
    private const val LIGHT_START_LIGHTNESS = 60

    private const val BLACK: Int = 0xFF000000.toInt()
    private const val WHITE: Int = 0xFFFFFFFF.toInt()

    fun on(backgroundColor: Int, minContrast: Double = NORMAL_TEXT_CONTRAST): Int {
        val isDarkContent = contrast(BLACK, backgroundColor) >= contrast(WHITE, backgroundColor)
        val (hue, saturation) = hueAndSaturation(backgroundColor)
        val targetContrast = if (saturation < GREY_SATURATION) {
            maxOf(minContrast, GREY_BACKGROUND_CONTRAST)
        } else {
            minContrast
        }
        val lightnessRange = if (isDarkContent) {
            DARK_START_LIGHTNESS downTo 0
        } else {
            LIGHT_START_LIGHTNESS..100
        }
        // Black or white at the end of the range always reach the contrast, see the tests
        return lightnessRange
            .asSequence()
            .map { lightness -> fromHsl(hue, saturation, lightness / 100.0) }
            .firstOrNull { color -> contrast(color, backgroundColor) >= targetContrast }
            ?: if (isDarkContent) BLACK else WHITE
    }

    /** WCAG contrast ratio of two opaque colors, from 1 to 21 */
    fun contrast(firstColor: Int, secondColor: Int): Double {
        val first = luminance(firstColor)
        val second = luminance(secondColor)
        return (maxOf(first, second) + 0.05) / (minOf(first, second) + 0.05)
    }

    /** Hue in degrees and saturation from 0 to 1 */
    fun hueAndSaturation(color: Int): Pair<Double, Double> {
        val r = red(color) / 255.0
        val g = green(color) / 255.0
        val b = blue(color) / 255.0
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        if (delta == 0.0) {
            return 0.0 to 0.0
        }
        val lightness = (max + min) / 2
        val saturation = delta / (1 - abs(2 * lightness - 1))
        val hue = when (max) {
            r -> 60 * (((g - b) / delta).mod(6.0))
            g -> 60 * ((b - r) / delta + 2)
            else -> 60 * ((r - g) / delta + 4)
        }
        return hue to saturation
    }

    private fun fromHsl(hue: Double, saturation: Double, lightness: Double): Int {
        val chroma = (1 - abs(2 * lightness - 1)) * saturation
        val x = chroma * (1 - abs((hue / 60).mod(2.0) - 1))
        val m = lightness - chroma / 2
        val (r, g, b) = when {
            hue < 60 -> Triple(chroma, x, 0.0)
            hue < 120 -> Triple(x, chroma, 0.0)
            hue < 180 -> Triple(0.0, chroma, x)
            hue < 240 -> Triple(0.0, x, chroma)
            hue < 300 -> Triple(x, 0.0, chroma)
            else -> Triple(chroma, 0.0, x)
        }
        fun channel(value: Double) = ((value + m) * 255).roundToInt().coerceIn(0, 255)
        return (0xFF shl 24) or (channel(r) shl 16) or (channel(g) shl 8) or channel(b)
    }

    private fun luminance(color: Int): Double {
        fun channel(value: Int): Double {
            val v = value / 255.0
            return if (v <= 0.03928) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(red(color)) + 0.7152 * channel(green(color)) + 0.0722 * channel(blue(color))
    }

    private fun red(color: Int) = (color shr 16) and 0xFF
    private fun green(color: Int) = (color shr 8) and 0xFF
    private fun blue(color: Int) = color and 0xFF
}
