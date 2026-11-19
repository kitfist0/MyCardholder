package my.cardholder.util.logo

import javax.inject.Inject

/** A logo offered for a card by its name, see [BrandCatalog.Brand.logo] */
data class LogoSuggestion(val name: String, val logo: String)

/**
 * Suggests logos of the [BrandCatalog] brands a card name may be about, e.g. "Пятёрочка"
 * for "пятер". Works offline and sends the card name nowhere.
 */
class LogoSuggester(
    private val brands: List<BrandCatalog.Brand>,
) {

    @Inject
    constructor() : this(BrandCatalog.brands)

    companion object {
        const val MAX_SUGGESTIONS = 5

        // One letter matches too many brands to help
        private const val MIN_QUERY_LENGTH = 2

        // A shorter brand name inside a longer card name is too likely a coincidence, e.g. "dm"
        private const val MIN_NAME_IN_QUERY_LENGTH = 4

        // Shorter names would match unrelated words without their last letter
        private const val MIN_STEMMED_NAME_LENGTH = 6
    }

    private enum class Match { WHOLE_NAME, NAME_START, WORD_START }

    private val searchableBrands = brands.map { brand ->
        brand to brand.names.map { name -> words(name) }
    }

    fun suggest(cardName: String): List<LogoSuggestion> {
        val queryWords = words(cardName)
        val query = queryWords.joinToString("")
        if (query.length < MIN_QUERY_LENGTH) {
            return emptyList()
        }
        return searchableBrands
            .mapNotNull { (brand, names) ->
                names.mapNotNull { nameWords -> match(query, queryWords, nameWords) }
                    .minOrNull()
                    ?.let { match -> brand to match }
            }
            // Stable, so equal matches keep the catalog order
            .sortedBy { (_, match) -> match }
            .take(MAX_SUGGESTIONS)
            .map { (brand, _) -> LogoSuggestion(name = brand.displayName, logo = brand.logo) }
    }

    private fun match(query: String, queryWords: List<String>, nameWords: List<String>): Match? {
        val name = nameWords.joinToString("")
        return when {
            name == query -> Match.WHOLE_NAME
            // Typed partly, e.g. "пятер"
            name.startsWith(query) -> Match.NAME_START
            // Inside a longer card name, e.g. "Пятёрочка Выручай-карта" or "Карта Пятёрочки"
            name.length >= MIN_NAME_IN_QUERY_LENGTH &&
                queryWords.indices.any { i -> queryWords.drop(i).joinToString("").startsWith(stem(name)) } ->
                Match.NAME_START
            // A later word of the name, e.g. "маркет" for "Яндекс Маркет"
            nameWords.indices.drop(1).any { i -> nameWords.drop(i).joinToString("").startsWith(query) } ->
                Match.WORD_START
            else -> null
        }
    }

    /** A longer name without its last letter, so that it matches its other forms, e.g. "Карта Пятёрочки" */
    private fun stem(name: String): String {
        return if (name.length >= MIN_STEMMED_NAME_LENGTH) name.dropLast(1) else name
    }

    /** Lowercase words without punctuation, so that "О'КЕЙ" is "окей" and "Ё" is "е" */
    private fun words(text: String): List<String> {
        return text.lowercase()
            .replace('ё', 'е')
            .replace("'", "")
            .replace("’", "")
            .split(Regex("[^\\p{L}\\p{N}]+"))
            .filter { it.isNotEmpty() }
    }
}
