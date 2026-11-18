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

    /**
     * Whether the point ([x], [y]) is within the scan frame of a [width]x[height] camera image
     * rotated by [rotationDegrees]. The point is in upright coordinates, as ML Kit returns them,
     * while the size is the one of the image before rotation, as the camera provides it.
     */
    fun isWithinScanFrame(x: Int, y: Int, width: Int, height: Int, rotationDegrees: Int): Boolean {
        val isRotatedSideways = rotationDegrees % 180 != 0
        val uprightWidth = if (isRotatedSideways) height else width
        val uprightHeight = if (isRotatedSideways) width else height
        val (left, top) = calculateScanFrameOffset(uprightWidth, uprightHeight)
        return x in left..(uprightWidth - left) && y in top..(uprightHeight - top)
    }
}
