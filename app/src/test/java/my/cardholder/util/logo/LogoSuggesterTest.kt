package my.cardholder.util.logo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LogoSuggesterTest {

    private val suggester = LogoSuggester()

    private fun logos(cardName: String) = suggester.suggest(cardName).map { it.logo }

    @Test
    fun `brand is found by the start of its name`() {
        assertEquals("5ka.ru", logos("пятер").first())
        assertEquals("5ka.ru", logos("Pyat").first())
    }

    @Test
    fun `letter case, yo and punctuation don't matter`() {
        assertEquals("5ka.ru", logos("ПЯТЕРОЧКА").first())
        assertEquals("okmarket.ru", logos("окей").first())
        assertEquals("krasnoeibeloe.ru", logos("Красное и белое").first())
        assertEquals("mvideo.ru", logos("мвидео").first())
    }

    @Test
    fun `brand is found inside a longer card name`() {
        assertEquals("5ka.ru", logos("Пятёрочка Выручай-карта").first())
        assertEquals("5ka.ru", logos("Карта Пятёрочки").first())
    }

    @Test
    fun `brand is found by a later word of its name`() {
        assertTrue(logos("маркет").contains("market.yandex.ru"))
    }

    @Test
    fun `exact name comes before names that only start with it`() {
        val suggester = LogoSuggester(
            listOf(
                BrandCatalog.Brand("long.example", listOf("Shopping")),
                BrandCatalog.Brand("short.example", listOf("Shop")),
            )
        )
        assertEquals(listOf("short.example", "long.example"), suggester.suggest("shop").map { it.logo })
    }

    @Test
    fun `short or unrelated names suggest nothing`() {
        assertEquals(emptyList<String>(), logos(""))
        assertEquals(emptyList<String>(), logos("п"))
        assertEquals(emptyList<String>(), logos("Библиотека"))
        // A short brand name inside a longer word is a coincidence: "dm" in "admin"
        assertEquals(emptyList<String>(), logos("Admin"))
    }

    @Test
    fun `suggestions are limited`() {
        assertTrue(logos("ма").size <= LogoSuggester.MAX_SUGGESTIONS)
    }

    @Test
    fun `suggestion shows the main brand name`() {
        val suggestion = suggester.suggest("Leroy Merlin").first()
        assertEquals("Лемана ПРО", suggestion.name)
        assertEquals("lemanapro.ru", suggestion.logo)
    }

    @Test
    fun `banks are found by their old names too`() {
        assertEquals("tbank.ru", logos("Тинькофф").first())
        assertEquals("sberbank.ru", logos("Сбербанк").first())
        assertEquals("halvacard.ru", logos("Халва").first())
    }

    @Test
    fun `catalog logos are unique`() {
        val logos = BrandCatalog.brands.map { it.logo }
        assertEquals(logos.distinct(), logos)
    }

    @Test
    fun `gas stations are found`() {
        assertEquals("bashneft-azs.ru", logos("Башнефть").first())
        assertEquals("https://teboil.ru/favicon.svg", logos("teboil").first())
    }
}
