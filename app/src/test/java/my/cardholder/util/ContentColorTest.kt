package my.cardholder.util

import my.cardholder.data.model.Card
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.min

class ContentColorTest {

    @Test
    fun `text on every standard card color has contrast of normal text`() {
        Card.COLORS.forEach { hex ->
            val cardColor = parse(hex)
            val contrast = ContentColor.contrast(ContentColor.on(cardColor), cardColor)
            assertTrue("$hex: %.2f".format(contrast), contrast >= ContentColor.NORMAL_TEXT_CONTRAST)
        }
    }

    @Test
    fun `text keeps the hue and saturation of the card`() {
        Card.COLORS.forEach { hex ->
            val (cardHue, cardSaturation) = ContentColor.hueAndSaturation(parse(hex))
            val text = ContentColor.on(parse(hex))
            val (textHue, textSaturation) = ContentColor.hueAndSaturation(text)
            // Near white or black, 8 bit channels can't keep the hue precisely, e.g. #F8F8FC
            if (cardSaturation > 0 && lightness(text) in 0.1..0.9) {
                assertTrue("$hex hue: $cardHue vs $textHue", hueDistance(cardHue, textHue) < 5)
                assertEquals("$hex saturation", cardSaturation, textSaturation, 0.06)
            }
        }
    }

    @Test
    fun `dark text is a deep shade of the card color`() {
        // As colored as the text of a Material button: e.g. navy on blue and ochre on yellow
        assertEquals("#053861", hex(ContentColor.on(parse("#42A5F5"))))
        assertEquals("#705500", hex(ContentColor.on(parse("#FFCA28"))))
        assertEquals("#4B0807", hex(ContentColor.on(parse("#EF5350"))))
    }

    @Test
    fun `light text is a pale shade of the card color`() {
        val (cardHue, _) = ContentColor.hueAndSaturation(parse("#7E57C2"))
        val text = ContentColor.on(parse("#7E57C2"))
        assertTrue(hex(text), text != parse("#FFFFFF"))
        assertTrue(hueDistance(cardHue, ContentColor.hueAndSaturation(text).first) < 5)
    }

    @Test
    fun `most mid toned background still gets readable text`() {
        // On these greys pure black and pure white give almost the same contrast, about 4.6:1,
        // which is the lowest the better of the two can be on any color
        listOf("#757575", "#767676", "#777777", "#787878").forEach { hex ->
            val background = parse(hex)
            val contrast = ContentColor.contrast(ContentColor.on(background), background)
            assertTrue("$hex: %.2f".format(contrast), contrast >= ContentColor.NORMAL_TEXT_CONTRAST)
        }
    }

    @Test
    fun `large text on every standard card color has contrast of large text`() {
        Card.COLORS.forEach { hex ->
            val cardColor = parse(hex)
            val contrast = ContentColor.contrast(ContentColor.on(cardColor, ContentColor.LARGE_TEXT_CONTRAST), cardColor)
            assertTrue("$hex: %.2f".format(contrast), contrast >= ContentColor.LARGE_TEXT_CONTRAST)
        }
    }

    @Test
    fun `large light text on mid tone cards keeps a visible tint`() {
        // Normal text on these is near white: #F4F0FA and #F8F8FC
        assertEquals("#CEBFE8", hex(ContentColor.on(parse("#7E57C2"), ContentColor.LARGE_TEXT_CONTRAST)))
        assertEquals("#C4CAE8", hex(ContentColor.on(parse("#5C6BC0"), ContentColor.LARGE_TEXT_CONTRAST)))
    }

    @Test
    fun `text on grey cards has enhanced contrast instead of looking dull`() {
        listOf("#424242", "#E0E0E0").forEach { hex ->
            val cardColor = parse(hex)
            listOf(ContentColor.NORMAL_TEXT_CONTRAST, ContentColor.LARGE_TEXT_CONTRAST).forEach { minContrast ->
                val contrast = ContentColor.contrast(ContentColor.on(cardColor, minContrast), cardColor)
                assertTrue("$hex: %.2f".format(contrast), contrast >= ContentColor.GREY_BACKGROUND_CONTRAST)
            }
        }
        // Was #B0B0B0 on the dark grey card
        assertEquals("#D9D9D9", hex(ContentColor.on(parse("#424242"))))
    }

    @Test
    fun `slightly colored cards keep the usual contrast and their tint`() {
        // Brown is not grey: its large text stays a tinted shade with the large text contrast
        val brown = parse("#8D6E63")
        assertEquals("#DBD0CC", hex(ContentColor.on(brown, ContentColor.LARGE_TEXT_CONTRAST)))
    }

    @Test
    fun `dark content is preferred on light cards and light content on dark cards`() {
        listOf("#FFCA28", "#E0E0E0", "#42A5F5", "#66BB6A").forEach { hex ->
            assertTrue(hex, ContentColor.prefersDarkContent(parse(hex)))
        }
        listOf("#424242", "#7E57C2", "#5C6BC0", "#8D6E63").forEach { hex ->
            assertTrue(hex, !ContentColor.prefersDarkContent(parse(hex)))
        }
    }

    @Test
    fun `contrast matches WCAG values`() {
        assertEquals(21.0, ContentColor.contrast(parse("#000000"), parse("#FFFFFF")), 0.01)
        assertEquals(1.0, ContentColor.contrast(parse("#42A5F5"), parse("#42A5F5")), 0.01)
        assertEquals(10.05, ContentColor.contrast(parse("#FFFFFF"), parse("#424242")), 0.01)
    }

    private fun lightness(color: Int): Double {
        val channels = listOf(16, 8, 0).map { shift -> ((color shr shift) and 0xFF) / 255.0 }
        return (channels.max() + channels.min()) / 2
    }

    private fun hueDistance(first: Double, second: Double): Double {
        val distance = abs(first - second) % 360
        return min(distance, 360 - distance)
    }

    private fun parse(hex: String): Int = (0xFF000000 or hex.removePrefix("#").toLong(16)).toInt()

    private fun hex(color: Int): String = "#%06X".format(color and 0xFFFFFF)
}
