package my.cardholder.util.logo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.math.hypot

class LogoBackgroundTest {

    private companion object {
        const val SIZE = 64
        const val YELLOW = 0xFFFFE600.toInt()
        const val BLACK = 0xFF000000.toInt()
        const val TRANSPARENT = 0
    }

    /** A logo drawn by [color] of each pixel */
    private fun logo(color: (x: Int, y: Int) -> Int) = IntArray(SIZE * SIZE) { i -> color(i % SIZE, i / SIZE) }

    private fun edgeColor(pixels: IntArray) = LogoBackground.edgeColor(pixels, SIZE, SIZE)

    private fun isInCenter(x: Int, y: Int) = x in 16 until 48 && y in 16 until 48

    @Test
    fun `logo on its own background gives its background color`() {
        // E.g. the black cross of Raiffeisen on a yellow square
        val pixels = logo { x, y -> if (isInCenter(x, y)) BLACK else YELLOW }

        assertEquals(YELLOW, edgeColor(pixels))
    }

    @Test
    fun `transparent rounded corners don't hide the background`() {
        val radius = 12
        val pixels = logo { x, y ->
            val cornerX = x.coerceIn(radius, SIZE - 1 - radius)
            val cornerY = y.coerceIn(radius, SIZE - 1 - radius)
            if (hypot((x - cornerX).toDouble(), (y - cornerY).toDouble()) > radius) TRANSPARENT else YELLOW
        }

        assertEquals(YELLOW, edgeColor(pixels))
    }

    @Test
    fun `round logo gets no background color`() {
        val center = SIZE / 2.0
        val pixels = logo { x, y -> if (hypot(x - center, y - center) < center) YELLOW else TRANSPARENT }

        assertNull(edgeColor(pixels))
    }

    @Test
    fun `sign without a background gets no background color`() {
        val pixels = logo { x, y -> if (isInCenter(x, y)) BLACK else TRANSPARENT }

        assertNull(edgeColor(pixels))
    }

    @Test
    fun `background of many colors gets no background color`() {
        // E.g. a photo or stripes
        val pixels = logo { x, _ -> if ((x / 4) % 2 == 0) YELLOW else BLACK }

        assertNull(edgeColor(pixels))
    }

    @Test
    fun `slightly uneven background still is one color`() {
        // E.g. JPEG noise
        val pixels = logo { x, y -> if ((x + y) % 2 == 0) YELLOW else 0xFFFDE402.toInt() }

        val color = edgeColor(pixels)!!
        assertEquals(0xFF, (color shr 16) and 0xFF, 2.0)
    }

    @Test
    fun `pale outermost line of a scaled logo doesn't hide its background`() {
        // As on the icon of Lidl: blue, with a line of two pale blues around it
        val blue = 0xFF0050AA.toInt()
        val pixels = logo { x, y ->
            when {
                y == 0 || y == SIZE - 1 -> 0xFFBFD3EA.toInt()
                x == 0 || x == SIZE - 1 -> 0xFFC4D6EB.toInt()
                else -> blue
            }
        }

        assertEquals(blue, edgeColor(pixels))
    }

    @Test
    fun `logo covering the middles of its sides still gives its background color`() {
        // As on the icon of Lidl: a yellow circle almost touching the sides of a blue square
        val blue = 0xFF0050AA.toInt()
        val center = SIZE / 2.0
        val pixels = logo { x, y -> if (hypot(x + 0.5 - center, y + 0.5 - center) < center - 1) YELLOW else blue }

        assertEquals(blue, edgeColor(pixels))
    }

    @Test
    fun `colors on both sides of a bucket border are one color`() {
        // 0x7F and 0x80 are in different buckets of 16
        val pixels = logo { x, _ -> if (x % 2 == 0) 0xFF7F7F7F.toInt() else 0xFF808080.toInt() }

        assertEquals(0x7F, edgeColor(pixels)!! and 0xFF, 1.0)
    }

    @Test
    fun `edge is read inside the logo but not in a tiny one`() {
        assertEquals(2, LogoBackground.edgeInset(60, 60))
        assertEquals(5, LogoBackground.edgeInset(128, 128))
        assertEquals(0, LogoBackground.edgeInset(8, 8))
    }

    private fun assertEquals(expected: Int, actual: Int, delta: Double) {
        assertEquals(expected.toDouble(), actual.toDouble(), delta)
    }
}
