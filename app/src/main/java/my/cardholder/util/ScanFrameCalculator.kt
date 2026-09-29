package my.cardholder.util

class ScanFrameCalculator {

    private companion object {
        const val SCAN_FRAME_SIZE_RATIO = 0.7f
    }

    /**
     * The (left, top) offset of the centered scan frame square (occupying [SCAN_FRAME_SIZE_RATIO]
     * of the smaller dimension) within a [width]x[height] area. The square's opposite corner is
     * at (width - left, height - top).
     */
    fun calculateScanFrameOffset(width: Int, height: Int): Pair<Int, Int> {
        val size = (minOf(width, height) * SCAN_FRAME_SIZE_RATIO).toInt()
        val left = (width - size) / 2
        val top = (height - size) / 2
        return left to top
    }
}
