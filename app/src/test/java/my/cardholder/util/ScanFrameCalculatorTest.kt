package my.cardholder.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanFrameCalculatorTest {

    private val calculator = ScanFrameCalculator()

    @Test
    fun `scan frame is centered square of 70 percent of smaller side`() {
        assertEquals(162 to 685, calculator.calculateScanFrameOffset(1080, 2126))
    }

    // A back camera in portrait gives 640x480 images rotated by 90 degrees, i.e. 480x640 upright,
    // where the scan frame spans 72..408 horizontally and 152..488 vertically

    @Test
    fun `point in lower part of frame of sideways image is within it`() {
        // Was outside: the frame was calculated as if the image were 640 wide and 480 high
        assertTrue(calculator.isWithinScanFrame(x = 240, y = 470, width = 640, height = 480, rotationDegrees = 90))
    }

    @Test
    fun `point in left part of frame of sideways image is within it`() {
        assertTrue(calculator.isWithinScanFrame(x = 90, y = 320, width = 640, height = 480, rotationDegrees = 270))
    }

    @Test
    fun `point above frame of sideways image is outside it`() {
        assertFalse(calculator.isWithinScanFrame(x = 240, y = 100, width = 640, height = 480, rotationDegrees = 90))
    }

    @Test
    fun `point right of frame of sideways image is outside it`() {
        assertFalse(calculator.isWithinScanFrame(x = 450, y = 320, width = 640, height = 480, rotationDegrees = 90))
    }

    @Test
    fun `upright image is not swapped`() {
        // 640x480 upright: the frame spans 152..488 horizontally and 72..408 vertically
        assertTrue(calculator.isWithinScanFrame(x = 470, y = 240, width = 640, height = 480, rotationDegrees = 0))
        assertFalse(calculator.isWithinScanFrame(x = 100, y = 240, width = 640, height = 480, rotationDegrees = 180))
    }
}
