package my.cardholder.util.logo

import kotlin.math.abs

/**
 * The color to put a logo on, so that the square around it continues the logo's own background.
 * Written without android.graphics, so that it runs in unit tests.
 */
object LogoBackground {

    // Of the edge, so that a logo on its own background (e.g. a yellow square) is taken by it.
    // Transparent rounded corners of a logo make up a small part of the edge.
    private const val MIN_OPAQUE_EDGE_SHARE = 0.75
    private const val MIN_SAME_COLOR_EDGE_SHARE = 0.6

    // Colors this close are one color, e.g. a gradient or JPEG noise along an edge
    private const val CHANNEL_BUCKET_SIZE = 16
    private const val MAX_SAME_COLOR_CHANNEL_DIFFERENCE = 24

    private const val OPAQUE_ALPHA = 0xF0

    // Of the logo size, see [edgeInset]
    private const val EDGE_INSET_SHARE = 1 / 24.0
    private const val MIN_EDGE_INSET = 2

    /**
     * How deep inside the logo its edge color is read. The outermost pixels of a scaled logo are
     * often a blend of its background with what was around it, e.g. a pale line around a blue
     * square, so they are not its color. Once the color is found, the logo is drawn without them.
     */
    fun edgeInset(width: Int, height: Int): Int {
        val inset = maxOf(MIN_EDGE_INSET, (minOf(width, height) * EDGE_INSET_SHARE).toInt())
        // A tiny logo has no edge to leave out
        return if (minOf(width, height) > inset * 4) inset else 0
    }

    /**
     * The color of the edge of the logo with [pixels] (ARGB, row by row), if its edge is
     * opaque and mostly of one color, and null otherwise. A logo with a transparent edge, e.g.
     * a round one or a sign without a background, needs a neutral background instead of its
     * own color, or it would vanish.
     */
    fun edgeColor(pixels: IntArray, width: Int, height: Int): Int? {
        if (width <= 0 || height <= 0 || pixels.size < width * height) {
            return null
        }
        val edge = edgePixels(pixels, width, height, edgeInset(width, height))
        val opaque = edge.filter { pixel -> (pixel ushr 24) >= OPAQUE_ALPHA }
        if (opaque.isEmpty() || opaque.size < edge.size * MIN_OPAQUE_EDGE_SHARE) {
            return null
        }
        // The most common color, with the colors close to it even across bucket borders
        val (_, mostCommon) = opaque.groupBy(::bucket).maxBy { (_, colors) -> colors.size }
        val candidate = average(mostCommon)
        val sameColor = opaque.filter { color -> isClose(color, candidate) }
        if (sameColor.size < edge.size * MIN_SAME_COLOR_EDGE_SHARE) {
            return null
        }
        return average(sameColor)
    }

    /**
     * The whole edge, as a logo may cover the middles of its sides, e.g. the circle of Lidl
     * on its blue square. On a round logo most of the edge is transparent.
     */
    private fun edgePixels(pixels: IntArray, width: Int, height: Int, inset: Int): List<Int> {
        val edge = ArrayList<Int>(2 * (width + height))
        val top = inset
        val bottom = height - 1 - inset
        val left = inset
        val right = width - 1 - inset
        for (x in left..right) {
            edge += pixels[top * width + x]
            edge += pixels[bottom * width + x]
        }
        for (y in top + 1 until bottom) {
            edge += pixels[y * width + left]
            edge += pixels[y * width + right]
        }
        return edge
    }

    private fun bucket(color: Int): Int {
        return listOf(16, 8, 0).fold(0) { key, shift ->
            key * 256 + ((color shr shift) and 0xFF) / CHANNEL_BUCKET_SIZE
        }
    }

    private fun isClose(first: Int, second: Int): Boolean {
        return listOf(16, 8, 0).all { shift ->
            abs(((first shr shift) and 0xFF) - ((second shr shift) and 0xFF)) <= MAX_SAME_COLOR_CHANNEL_DIFFERENCE
        }
    }

    private fun average(colors: List<Int>): Int {
        fun channel(shift: Int) = colors.sumOf { (it shr shift) and 0xFF } / colors.size
        return (0xFF shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
    }
}
