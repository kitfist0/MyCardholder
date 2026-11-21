package my.cardholder.data

import my.cardholder.data.model.Brand
import my.cardholder.data.source.BrandCatalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrandRepositoryTest {

    private val brandRepository = BrandRepository()

    private fun logos(cardName: String) = brandRepository.suggestLogos(cardName).map { it.logo }

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
        val brandRepository = BrandRepository(
            listOf(
                Brand("long.example", listOf("Shopping")),
                Brand("short.example", listOf("Shop")),
            )
        )
        assertEquals(listOf("short.example", "long.example"), brandRepository.suggestLogos("shop").map { it.logo })
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
        assertTrue(logos("ма").size <= BrandRepository.MAX_SUGGESTIONS)
    }

    @Test
    fun `suggestion shows the main brand name`() {
        val suggestion = brandRepository.suggestLogos("Leroy Merlin").first()
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
    fun `foreign banks are found by their names in both alphabets`() {
        assertEquals("kaspi.kz", logos("Каспи").first())
        assertEquals("revolut.com", logos("Revolut").first())
        assertEquals("tbcbank.ge", logos("TBC").first())
    }

    @Test
    fun `coffee shops are found`() {
        assertEquals("surfcoffee.ru", logos("Surf").first())
        assertEquals("coffeemania.ru", logos("Кофемания").first())
        assertEquals("costa.co.uk", logos("Коста").first())
    }

    @Test
    fun `international brands are found by their names in both alphabets`() {
        assertEquals("nike.com", logos("Найк").first())
        assertEquals("turkishairlines.com", logos("Turkish").first())
        assertEquals("pullandbear.com", logos("Pull and Bear").first())
    }

    @Test
    fun `sports stores are found`() {
        assertEquals("kant.ru", logos("Кант").first())
        assertEquals("jdsports.com", logos("JD Sports").first())
        assertTrue(logos("Декатлон").contains("desport.ru"))
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
