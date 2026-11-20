package my.cardholder.util.logo

/**
 * The color to put a logo on, so that the square around it continues the logo's own background.
 * Written without android.graphics, so that it runs in unit tests.
 */
object LogoBackground {

    // Of the edge, so that a logo on its own background (e.g. a yellow square) is taken by it
    private const val MIN_OPAQUE_EDGE_SHARE = 0.9
    private const val MIN_SAME_COLOR_EDGE_SHARE = 0.6

    // Colors this close are one color, e.g. a gradient or JPEG noise along an edge
    private const val CHANNEL_BUCKET_SIZE = 16

    private const val OPAQUE_ALPHA = 0xF0

    // Of each side, left out at both ends
    private const val CORNER_SHARE = 0.25

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
        val edge = edgePixels(pixels, width, height)
        val opaque = edge.filter { pixel -> (pixel ushr 24) >= OPAQUE_ALPHA }
        if (opaque.size < edge.size * MIN_OPAQUE_EDGE_SHARE) {
            return null
        }
        val (_, sameColor) = opaque.groupBy(::bucket).maxBy { (_, colors) -> colors.size }
        if (sameColor.size < edge.size * MIN_SAME_COLOR_EDGE_SHARE) {
            return null
        }
        return average(sameColor)
    }

    /**
     * The middle of each side: many logos have transparent rounded corners, though their
     * background is opaque. On a round logo the middles are still mostly transparent.
     */
    private fun edgePixels(pixels: IntArray, width: Int, height: Int): List<Int> {
        val edge = ArrayList<Int>(2 * (width + height))
        for (x in middle(width)) {
            edge += pixels[x]
            edge += pixels[(height - 1) * width + x]
        }
        for (y in middle(height)) {
            edge += pixels[y * width]
            edge += pixels[y * width + width - 1]
        }
        return edge
    }

    private fun middle(length: Int): IntRange {
        val corner = (length * CORNER_SHARE).toInt()
        return corner until length - corner
    }

    private fun bucket(color: Int): Int {
        return listOf(16, 8, 0).fold(0) { key, shift ->
            key * 256 + ((color shr shift) and 0xFF) / CHANNEL_BUCKET_SIZE
        }
    }

    private fun average(colors: List<Int>): Int {
        fun channel(shift: Int) = colors.sumOf { (it shr shift) and 0xFF } / colors.size
        return (0xFF shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
    }
}
