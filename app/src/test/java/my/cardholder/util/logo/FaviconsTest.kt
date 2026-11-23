package my.cardholder.util.logo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FaviconsTest {

    @Test
    fun `website is taken from any address`() {
        assertEquals("lukoil.ru", Favicons.website("lukoil.ru"))
        assertEquals("lukoil.ru", Favicons.website(" https://lukoil.ru/azs?city=1 "))
        assertEquals("www.rzd.ru", Favicons.website("http://www.rzd.ru"))
        assertNull(Favicons.website(""))
        assertNull(Favicons.website("https://"))
    }

    @Test
    fun `sharp icon of Google is chosen without Yandex`() {
        assertEquals(Favicons.Source.GOOGLE, Favicons.choose(googleSizePx = 128, yandexSizePx = null))
        assertEquals(Favicons.Source.GOOGLE, Favicons.choose(googleSizePx = 32, yandexSizePx = 120))
    }

    @Test
    fun `icon of Yandex is chosen instead of a blurry or missing icon of Google`() {
        // E.g. lukoil.ru and rosneft.ru
        assertEquals(Favicons.Source.YANDEX, Favicons.choose(googleSizePx = 16, yandexSizePx = 120))
        assertEquals(Favicons.Source.YANDEX, Favicons.choose(googleSizePx = null, yandexSizePx = 120))
    }

    @Test
    fun `blurry icon of Google is better than no icon of Yandex`() {
        // Yandex gives a 1px image for a site it has no icon of
        assertEquals(Favicons.Source.GOOGLE, Favicons.choose(googleSizePx = 16, yandexSizePx = 1))
        assertEquals(Favicons.Source.GOOGLE, Favicons.choose(googleSizePx = 16, yandexSizePx = null))
    }

    @Test
    fun `site without icons has no icon`() {
        assertNull(Favicons.choose(googleSizePx = null, yandexSizePx = 1))
        assertNull(Favicons.choose(googleSizePx = null, yandexSizePx = null))
    }
}
